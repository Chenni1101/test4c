package edu.suibe.evidence.service;

import edu.suibe.evidence.config.ChainProperties;
import edu.suibe.evidence.dto.AssetCredentialResponse;
import edu.suibe.evidence.dto.AssetIssueRequest;
import edu.suibe.evidence.dto.ChainTransactionResponse;
import edu.suibe.evidence.dto.IpfsFileResponse;
import edu.suibe.evidence.entity.AssetEntity;
import edu.suibe.evidence.entity.AssetVersionEntity;
import edu.suibe.evidence.entity.ChainTransactionEntity;
import edu.suibe.evidence.entity.EvidenceRecordEntity;
import edu.suibe.evidence.entity.IpfsFileEntity;
import edu.suibe.evidence.repository.AssetRepository;
import edu.suibe.evidence.repository.AssetVersionRepository;
import edu.suibe.evidence.repository.ChainTransactionRepository;
import edu.suibe.evidence.repository.EvidenceRecordRepository;
import edu.suibe.evidence.repository.IpfsFileRepository;
import edu.suibe.evidence.security.CurrentUserProvider;
import edu.suibe.evidence.security.UserAccountPrincipal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetIssuanceService {
  private final AssetRepository assetRepository;
  private final AssetVersionRepository assetVersionRepository;
  private final IpfsFileRepository ipfsFileRepository;
  private final ChainTransactionRepository chainTransactionRepository;
  private final EvidenceRecordRepository evidenceRecordRepository;
  private final FallbackIpfsStorageService ipfsStorageService;
  private final BlockchainGateway blockchainGateway;
  private final AssetCodeGenerator assetCodeGenerator;
  private final CurrentUserProvider currentUserProvider;
  private final AuditService auditService;
  private final ChainProperties chainProperties;

  public AssetIssuanceService(
      AssetRepository assetRepository,
      AssetVersionRepository assetVersionRepository,
      IpfsFileRepository ipfsFileRepository,
      ChainTransactionRepository chainTransactionRepository,
      EvidenceRecordRepository evidenceRecordRepository,
      FallbackIpfsStorageService ipfsStorageService,
      BlockchainGateway blockchainGateway,
      AssetCodeGenerator assetCodeGenerator,
      CurrentUserProvider currentUserProvider,
      AuditService auditService,
      ChainProperties chainProperties) {
    this.assetRepository = assetRepository;
    this.assetVersionRepository = assetVersionRepository;
    this.ipfsFileRepository = ipfsFileRepository;
    this.chainTransactionRepository = chainTransactionRepository;
    this.evidenceRecordRepository = evidenceRecordRepository;
    this.ipfsStorageService = ipfsStorageService;
    this.blockchainGateway = blockchainGateway;
    this.assetCodeGenerator = assetCodeGenerator;
    this.currentUserProvider = currentUserProvider;
    this.auditService = auditService;
    this.chainProperties = chainProperties;
  }

  @Transactional
  public AssetCredentialResponse issue(AssetIssueRequest request, String idempotencyKey) {
    UserAccountPrincipal actor = currentUserProvider.requireUser();
    assertCanIssue(actor);
    ChainTransactionEntity existing = chainTransactionRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
    if (existing != null) return credentialFor(existing, actor);

    String digest = normalizeHash(request.contentSha256());
    if (assetRepository.findByFileHash("sha256:" + digest).isPresent()) {
      throw new DataIntegrityViolationException("该内容摘要已经发行；请使用原请求的 Idempotency-Key 获取结果");
    }
    Instant now = Instant.now();
    AssetEntity asset = new AssetEntity();
    asset.setAssetCode(assetCodeGenerator.nextCode());
    asset.setAssetName(request.assetName());
    asset.setAssetType(request.assetType());
    asset.setCreator(request.creator());
    asset.setOrganization(resolveOrganization(request.organization(), actor));
    asset.setOwnerUserId(actor.getId());
    asset.setDescription(request.description());
    asset.setFileHash("sha256:" + digest);
    asset.setStatus("PENDING_CHAIN");
    asset.setCreatedAt(now);
    asset = assetRepository.save(asset);

    AssetVersionEntity version = new AssetVersionEntity();
    version.setAssetId(asset.getId());
    version.setVersionNo(1);
    version.setContentSha256(digest);
    version.setOriginalFilename(request.originalFilename());
    version.setMimeType(request.mimeType());
    version.setFileSizeBytes(request.fileSizeBytes());
    version.setStorageStatus("PENDING");
    version.setStatus("PENDING_CHAIN");
    version.setSubmittedByUserId(actor.getId());
    version.setMetadataSnapshot("{\"hashSource\":\"CLIENT_DECLARED\"}");
    version = assetVersionRepository.save(version);

    ChainTransactionEntity transaction = pendingTransaction(asset, version, idempotencyKey, now);
    transaction = chainTransactionRepository.save(transaction);

    IpfsStoreResult storage;
    try {
      storage = ipfsStorageService.store(new IpfsStoreRequest(digest, request.originalFilename(), request.mimeType(), request.fileSizeBytes()));
    } catch (RuntimeException error) {
      version.setStorageStatus("FAILED");
      version.setStatus("DRAFT");
      asset.setStatus("DRAFT");
      fail(transaction, "IPFS_STORE_FAILED", error.getMessage());
      auditService.record("ASSET_ISSUE_IPFS_FAILED", actor.getUsername(), asset.getAssetCode(), "provider-unavailable");
      return credential(asset, version, null, transaction, "IPFS 存储失败，未提交链交易；可修复配置后重新发行");
    }

    IpfsFileEntity file = new IpfsFileEntity();
    file.setAssetVersionId(version.getId());
    file.setProvider(storage.provider());
    file.setMode(storage.mode());
    file.setCid(storage.cid());
    file.setGatewayUrl(storage.gatewayUrl());
    file.setContentSha256(digest);
    file.setSizeBytes(request.fileSizeBytes());
    file.setPinStatus(storage.status());
    ipfsFileRepository.save(file);
    version.setStorageStatus("STORED");

    submitChain(asset, version, file, transaction, actor);
    return credential(asset, version, file, transaction, noticeFor(transaction));
  }

  @Transactional
  public List<AssetCredentialResponse> issueBatch(List<AssetIssueRequest> requests, String batchIdempotencyKey) {
    if (requests.size() > 10) throw new IllegalArgumentException("单次最多发行 10 件资产");
    return java.util.stream.IntStream.range(0, requests.size())
        .mapToObj(index -> issue(requests.get(index), batchIdempotencyKey + "-" + index))
        .toList();
  }

  @Transactional
  public AssetCredentialResponse retry(Long transactionId) {
    UserAccountPrincipal actor = currentUserProvider.requireUser();
    ChainTransactionEntity transaction = chainTransactionRepository.findById(transactionId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("链交易不存在"));
    if (!"FAILED".equals(transaction.getStatus())) throw new IllegalStateException("仅失败交易可重试");
    AssetVersionEntity version = assetVersionRepository.findById(transaction.getAssetVersionId()).orElseThrow();
    AssetEntity asset = assetRepository.findById(version.getAssetId()).orElseThrow();
    assertCanManage(asset, actor);
    IpfsFileEntity file = ipfsFileRepository.findByAssetVersionId(version.getId()).stream().findFirst().orElseThrow(() -> new IllegalStateException("无法重试：没有已保存的存储记录"));
    transaction.setAttemptNo((transaction.getAttemptNo() == null ? 0 : transaction.getAttemptNo()) + 1);
    transaction.setStatus("PENDING");
    transaction.setErrorCode(null);
    transaction.setErrorMessage(null);
    submitChain(asset, version, file, transaction, actor);
    return credential(asset, version, file, transaction, noticeFor(transaction));
  }

  @Transactional(readOnly = true)
  public IpfsFileResponse findCid(String cid) {
    IpfsFileEntity file = ipfsFileRepository.findByCid(cid).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("未找到该 CID 记录"));
    AssetVersionEntity version = assetVersionRepository.findById(file.getAssetVersionId()).orElseThrow();
    AssetEntity asset = assetRepository.findById(version.getAssetId()).orElseThrow();
    assertCanManage(asset, currentUserProvider.requireUser());
    return new IpfsFileResponse(file.getId(), file.getAssetVersionId(), file.getCid(), file.getProvider(), file.getMode(), file.getPinStatus(), file.getContentSha256(), noticeFor(file));
  }

  @Transactional(readOnly = true)
  public ChainTransactionResponse findTransaction(Long id) {
    ChainTransactionEntity transaction = chainTransactionRepository.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("链交易不存在"));
    AssetEntity asset = assetFor(transaction);
    assertCanManage(asset, currentUserProvider.requireUser());
    return transactionResponse(transaction);
  }

  private void submitChain(
      AssetEntity asset,
      AssetVersionEntity version,
      IpfsFileEntity file,
      ChainTransactionEntity transaction,
      UserAccountPrincipal actor) {
    try {
      ChainReceipt receipt = blockchainGateway.issueAsset(
          new ChainAssetIssueRequest(asset.getAssetCode(), version.getContentSha256(), file.getCid(), actor.getUsername(), Instant.now().toString()));
      transaction.setTxId(receipt.txId());
      transaction.setProvider(receipt.provider());
      transaction.setMode(receipt.mode());
      transaction.setNetwork(chainProperties.getNetwork());
      transaction.setResponsePayload(receipt.responsePayload());
      transaction.setStatus(receipt.status());
      transaction.setChainBlockHeight(receipt.blockHeight());
      transaction.setConfirmedAt(Instant.now());
      transaction.setUpdatedAt(Instant.now());
      version.setStatus("CERTIFIED");
      version.setImmutableAt(Instant.now());
      asset.setStatus("CERTIFIED");
      saveEvidenceIfAbsent(asset, version, receipt, actor);
      auditService.record("ASSET_ISSUE_CONFIRMED", actor.getUsername(), asset.getAssetCode(), "mode=" + receipt.mode());
    } catch (RuntimeException error) {
      asset.setStatus("PENDING_CHAIN");
      version.setStatus("PENDING_CHAIN");
      fail(transaction, "CHAIN_SUBMISSION_FAILED", error.getMessage());
      auditService.record("ASSET_ISSUE_CHAIN_FAILED", actor.getUsername(), asset.getAssetCode(), "retry-available");
    }
  }

  private ChainTransactionEntity pendingTransaction(AssetEntity asset, AssetVersionEntity version, String key, Instant now) {
    ChainTransactionEntity transaction = new ChainTransactionEntity();
    transaction.setTxId("pending_" + java.util.UUID.randomUUID());
    transaction.setFileHash(asset.getFileHash());
    transaction.setAssetVersionId(version.getId());
    transaction.setContractName(chainProperties.getContractName());
    transaction.setMethodName("issueAsset");
    transaction.setProvider("PENDING");
    transaction.setMode(chainProperties.getMode());
    transaction.setNetwork(chainProperties.getNetwork());
    transaction.setIdempotencyKey(key);
    transaction.setRequestDigest(sha256(asset.getAssetCode() + version.getContentSha256()));
    transaction.setRequestPayload("{\"assetCode\":\"" + asset.getAssetCode() + "\",\"contentSha256\":\"" + version.getContentSha256() + "\"}");
    transaction.setResponsePayload("{}");
    transaction.setStatus("PENDING");
    transaction.setAttemptNo(1);
    transaction.setCreatedAt(now);
    transaction.setUpdatedAt(now);
    return transaction;
  }

  private void saveEvidenceIfAbsent(AssetEntity asset, AssetVersionEntity version, ChainReceipt receipt, UserAccountPrincipal actor) {
    if (evidenceRecordRepository.findByFileHash(asset.getFileHash()).isPresent()) return;
    EvidenceRecordEntity evidence = new EvidenceRecordEntity();
    evidence.setAssetId(asset.getId());
    evidence.setFileHash(asset.getFileHash());
    evidence.setOwnerName(actor.getUsername());
    evidence.setTimestampIso(Instant.now().toString());
    evidence.setChainNetwork(chainProperties.getNetwork());
    evidence.setContractName(chainProperties.getContractName());
    evidence.setTxId(receipt.txId());
    evidence.setBlockHeight(receipt.blockHeight());
    evidence.setCreatedAt(Instant.now());
    evidenceRecordRepository.save(evidence);
  }

  private void fail(ChainTransactionEntity transaction, String code, String message) {
    transaction.setStatus("FAILED");
    transaction.setErrorCode(code);
    transaction.setErrorMessage(message == null ? "外部服务调用失败" : message.substring(0, Math.min(message.length(), 1000)));
    transaction.setUpdatedAt(Instant.now());
  }

  private AssetCredentialResponse credentialFor(ChainTransactionEntity transaction, UserAccountPrincipal actor) {
    AssetEntity asset = assetFor(transaction);
    assertCanManage(asset, actor);
    AssetVersionEntity version = assetVersionRepository.findById(transaction.getAssetVersionId()).orElseThrow();
    IpfsFileEntity file = ipfsFileRepository.findByAssetVersionId(version.getId()).stream().findFirst().orElse(null);
    return credential(asset, version, file, transaction, noticeFor(transaction));
  }

  private AssetCredentialResponse credential(AssetEntity asset, AssetVersionEntity version, IpfsFileEntity file, ChainTransactionEntity transaction, String notice) {
    return new AssetCredentialResponse(
        asset.getAssetCode(), asset.getId(), version.getId(), "sha256:" + version.getContentSha256(),
        file == null ? null : file.getCid(), file == null ? null : file.getProvider(), file == null ? null : file.getMode(),
        String.valueOf(transaction.getId()), transaction.getTxId(), transaction.getStatus(), asset.getStatus(),
        transaction.getMode(), notice);
  }

  private ChainTransactionResponse transactionResponse(ChainTransactionEntity transaction) {
    return new ChainTransactionResponse(
        transaction.getId(), transaction.getAssetVersionId(), transaction.getTxId(), transaction.getStatus(), transaction.getProvider(),
        transaction.getMode(), transaction.getNetwork(), transaction.getAttemptNo(), transaction.getChainBlockHeight(), transaction.getConfirmedAt(),
        transaction.getErrorCode(), transaction.getErrorMessage(), transaction.getResponsePayload());
  }

  private AssetEntity assetFor(ChainTransactionEntity transaction) {
    if (transaction.getAssetVersionId() != null) {
      AssetVersionEntity version = assetVersionRepository.findById(transaction.getAssetVersionId()).orElseThrow();
      return assetRepository.findById(version.getAssetId()).orElseThrow();
    }
    return assetRepository.findByFileHash(transaction.getFileHash()).orElseThrow();
  }

  private void assertCanIssue(UserAccountPrincipal actor) {
    if (actor.hasRole("CREATOR") || actor.hasRole("MUSEUM_ADMIN") || actor.hasRole("SUPER_ADMIN")) return;
    throw new AccessDeniedException("当前角色不能发行资产");
  }

  private void assertCanManage(AssetEntity asset, UserAccountPrincipal actor) {
    if (actor.hasRole("SUPER_ADMIN")) return;
    if (actor.hasRole("MUSEUM_ADMIN") && actor.getOrganization() != null && actor.getOrganization().equals(asset.getOrganization())) return;
    if (actor.hasRole("CREATOR") && actor.getId().equals(asset.getOwnerUserId())) return;
    throw new AccessDeniedException("无权访问该资产发行记录");
  }

  private String resolveOrganization(String requested, UserAccountPrincipal actor) {
    if (actor.hasRole("MUSEUM_ADMIN")) {
      if (actor.getOrganization() == null || actor.getOrganization().isBlank()) throw new IllegalStateException("文博管理员必须关联机构");
      return actor.getOrganization();
    }
    if (actor.hasRole("CREATOR") && actor.getOrganization() != null && !actor.getOrganization().isBlank()) return actor.getOrganization();
    return requested;
  }

  private String normalizeHash(String value) {
    return value.replaceFirst("(?i)^sha256:", "").toLowerCase(Locale.ROOT);
  }

  private String sha256(String text) {
    try {
      byte[] bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder();
      for (byte value : bytes) result.append(String.format("%02x", value));
      return result.toString();
    } catch (Exception error) {
      throw new IllegalStateException("SHA-256 不可用", error);
    }
  }

  private String noticeFor(ChainTransactionEntity transaction) {
    return "DEMO".equals(transaction.getMode())
        ? "演示模式：CID 与链交易回执均为 Demo/Mock 标识，不代表真实 IPFS 或真实链上交易。"
        : "真实模式：请通过配置的链浏览器与 IPFS 网关复核。";
  }

  private String noticeFor(IpfsFileEntity file) {
    return "DEMO".equals(file.getMode()) ? "演示模式：该 demo-cid-* 不是真实 IPFS CID。" : "真实 IPFS 存储记录。";
  }
}
