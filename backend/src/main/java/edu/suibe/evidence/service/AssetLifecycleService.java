package edu.suibe.evidence.service;

import edu.suibe.evidence.config.ChainProperties;
import edu.suibe.evidence.dto.AssetVersionCreateRequest;
import edu.suibe.evidence.dto.AssetVersionResponse;
import edu.suibe.evidence.dto.AuthorizationCreateRequest;
import edu.suibe.evidence.dto.AuthorizationResponse;
import edu.suibe.evidence.dto.FileHashVerificationResponse;
import edu.suibe.evidence.dto.ProvenanceReportResponse;
import edu.suibe.evidence.dto.TimelineEventResponse;
import edu.suibe.evidence.entity.AssetEntity;
import edu.suibe.evidence.entity.AssetVersionEntity;
import edu.suibe.evidence.entity.AuthorizationEntity;
import edu.suibe.evidence.entity.ChainTransactionEntity;
import edu.suibe.evidence.entity.IpfsFileEntity;
import edu.suibe.evidence.repository.AssetRepository;
import edu.suibe.evidence.repository.AssetVersionRepository;
import edu.suibe.evidence.repository.AuthorizationRepository;
import edu.suibe.evidence.repository.ChainTransactionRepository;
import edu.suibe.evidence.repository.EvidenceRecordRepository;
import edu.suibe.evidence.repository.IpfsFileRepository;
import edu.suibe.evidence.security.CurrentUserProvider;
import edu.suibe.evidence.security.UserAccountPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AssetLifecycleService {
  private final AssetRepository assets;
  private final AssetVersionRepository versions;
  private final AuthorizationRepository authorizations;
  private final IpfsFileRepository ipfsFiles;
  private final ChainTransactionRepository chainTransactions;
  private final EvidenceRecordRepository evidenceRecords;
  private final CurrentUserProvider currentUser;
  private final AuthorizationCodeGenerator authorizationCodeGenerator;
  private final FallbackIpfsStorageService ipfsStorage;
  private final BlockchainGateway blockchainGateway;
  private final ChainProperties chainProperties;
  private final AuditService audit;

  public AssetLifecycleService(
      AssetRepository assets, AssetVersionRepository versions, AuthorizationRepository authorizations,
      IpfsFileRepository ipfsFiles, ChainTransactionRepository chainTransactions,
      EvidenceRecordRepository evidenceRecords, CurrentUserProvider currentUser,
      AuthorizationCodeGenerator authorizationCodeGenerator, FallbackIpfsStorageService ipfsStorage,
      BlockchainGateway blockchainGateway, ChainProperties chainProperties, AuditService audit) {
    this.assets = assets; this.versions = versions; this.authorizations = authorizations;
    this.ipfsFiles = ipfsFiles; this.chainTransactions = chainTransactions; this.evidenceRecords = evidenceRecords;
    this.currentUser = currentUser; this.authorizationCodeGenerator = authorizationCodeGenerator;
    this.ipfsStorage = ipfsStorage; this.blockchainGateway = blockchainGateway;
    this.chainProperties = chainProperties; this.audit = audit;
  }

  @Transactional
  public AuthorizationResponse createAuthorization(AuthorizationCreateRequest request) {
    expireDueAuthorizations();
    UserAccountPrincipal actor = currentUser.requireUser();
    AssetEntity asset = assetByCode(request.assetCode());
    assertCanManage(asset, actor);
    AssetVersionEntity version = resolveVersion(asset, request.assetVersionId());
    if (!"CERTIFIED".equals(version.getStatus())) throw new IllegalStateException("仅已认证版本可发起授权");
    if (!request.commercial() && request.revenueShareRatio().compareTo(BigDecimal.ZERO) != 0) {
      throw new IllegalArgumentException("非商用授权的分润比例必须为 0");
    }
    if ((request.commercial() && !"COMMERCIAL".equals(request.usageType()))
        || (!request.commercial() && !"NON_COMMERCIAL".equals(request.usageType()))) {
      throw new IllegalArgumentException("commercial 必须与 usageType 一致");
    }
    if (!request.endsAt().isAfter(request.startsAt())) throw new IllegalArgumentException("授权结束时间必须晚于开始时间");
    rejectDuplicate(asset.getId(), request, version.getId());

    AuthorizationEntity authorization = new AuthorizationEntity();
    authorization.setAuthorizationNo(authorizationCodeGenerator.nextCode());
    authorization.setAssetId(asset.getId()); authorization.setAssetVersionId(version.getId());
    authorization.setLicensorUserId(actor.getId()); authorization.setLicenseeName(request.licenseeName().trim());
    authorization.setLicenseeOrganization(blankToNull(request.licenseeOrganization()));
    authorization.setUsageType(request.usageType()); authorization.setCommercial(request.commercial());
    authorization.setRevenueShareRatio(request.revenueShareRatio()); authorization.setStartsAt(request.startsAt());
    authorization.setEndsAt(request.endsAt()); authorization.setStatus("DRAFT");
    authorization = authorizations.save(authorization);

    ChainTransactionEntity transaction = authorizationTransaction(asset, version, authorization);
    transaction = chainTransactions.save(transaction);
    try {
      ChainReceipt receipt = blockchainGateway.recordAuthorization(new ChainAuthorizationRequest(
          authorization.getAuthorizationNo(), asset.getAssetCode(), version.getId(), authorization.getLicenseeName(),
          authorization.getUsageType(), authorization.isCommercial(), authorization.getStartsAt().toString(),
          authorization.getEndsAt().toString(), actor.getUsername()));
      transaction.setTxId(receipt.txId()); transaction.setProvider(receipt.provider()); transaction.setMode(receipt.mode());
      transaction.setStatus(receipt.status()); transaction.setChainBlockHeight(receipt.blockHeight());
      transaction.setResponsePayload(receipt.responsePayload()); transaction.setConfirmedAt(Instant.now()); transaction.setUpdatedAt(Instant.now());
      authorization.setChainTransactionId(transaction.getId()); authorization.setChainTxId(receipt.txId()); authorization.setChainMode(receipt.mode());
      authorization.setStatus("ACTIVE");
      recomputeAssetStatus(asset);
      audit.record("AUTHORIZATION_ISSUED", actor.getUsername(), authorization.getAuthorizationNo(), "asset=" + asset.getAssetCode() + ";mode=" + receipt.mode());
    } catch (RuntimeException error) {
      transaction.setStatus("FAILED"); transaction.setErrorCode("AUTHORIZATION_CHAIN_FAILED");
      transaction.setErrorMessage(safeMessage(error)); transaction.setUpdatedAt(Instant.now());
      audit.record("AUTHORIZATION_CHAIN_FAILED", actor.getUsername(), authorization.getAuthorizationNo(), "asset=" + asset.getAssetCode());
    }
    return response(authorization, asset);
  }

  @Transactional
  public List<AuthorizationResponse> createAuthorizations(List<AuthorizationCreateRequest> requests) {
    if (requests.size() > 10) throw new IllegalArgumentException("单次最多创建 10 条授权");
    return requests.stream().map(this::createAuthorization).toList();
  }

  @Transactional
  public AuthorizationResponse revoke(String authorizationId, String reason) {
    expireDueAuthorizations();
    UserAccountPrincipal actor = currentUser.requireUser();
    AuthorizationEntity authorization = authorizations.findById(authorizationId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("授权记录不存在"));
    AssetEntity asset = assets.findById(authorization.getAssetId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("授权资产不存在"));
    assertCanManage(asset, actor);
    if (!"ACTIVE".equals(authorization.getStatus())) throw new IllegalStateException("仅有效授权可提前撤销");
    authorization.setStatus("REVOKED"); authorization.setRevokedAt(Instant.now()); authorization.setRevokeReason(reason.trim());
    recomputeAssetStatus(asset);
    audit.record("AUTHORIZATION_REVOKED", actor.getUsername(), authorization.getAuthorizationNo(), "asset=" + asset.getAssetCode());
    return response(authorization, asset);
  }

  @Transactional
  public List<AuthorizationResponse> list(String assetCode) {
    expireDueAuthorizations();
    AssetEntity asset = assetByCode(assetCode);
    assertCanManage(asset, currentUser.requireUser());
    return authorizations.findByAssetIdOrderByCreatedAtDesc(asset.getId()).stream().map(item -> response(item, asset)).toList();
  }

  @Transactional(readOnly = true)
  public List<AssetVersionResponse> listVersions(String assetCode) {
    AssetEntity asset = assetByCode(assetCode); assertCanManage(asset, currentUser.requireUser());
    return versions.findByAssetIdOrderByVersionNoDesc(asset.getId()).stream().map(item -> versionResponse(asset, item)).toList();
  }

  @Transactional
  public AssetVersionResponse createVersion(String assetCode, AssetVersionCreateRequest request) {
    UserAccountPrincipal actor = currentUser.requireUser(); AssetEntity asset = assetByCode(assetCode); assertCanManage(asset, actor);
    String hash = normalizeHash(request.contentSha256());
    if (versions.existsByAssetIdAndContentSha256(asset.getId(), hash)) throw new IllegalStateException("该资产已存在相同内容摘要的版本");
    int nextVersion = versions.findByAssetIdOrderByVersionNoDesc(asset.getId()).stream().mapToInt(AssetVersionEntity::getVersionNo).max().orElse(0) + 1;
    AssetVersionEntity version = new AssetVersionEntity();
    version.setAssetId(asset.getId()); version.setVersionNo(nextVersion); version.setContentSha256(hash);
    version.setOriginalFilename(request.originalFilename()); version.setMimeType(request.mimeType()); version.setFileSizeBytes(request.fileSizeBytes());
    version.setChangeNote(blankToNull(request.changeNote())); version.setMetadataSnapshot("{\"hashSource\":\"CLIENT_DECLARED\"}");
    version.setStorageStatus("PENDING"); version.setStatus("DRAFT"); version.setSubmittedByUserId(actor.getId()); version = versions.save(version);
    try {
      IpfsStoreResult stored = ipfsStorage.store(new IpfsStoreRequest(hash, request.originalFilename(), request.mimeType(), request.fileSizeBytes()));
      IpfsFileEntity file = new IpfsFileEntity(); file.setAssetVersionId(version.getId()); file.setProvider(stored.provider()); file.setMode(stored.mode());
      file.setCid(stored.cid()); file.setGatewayUrl(stored.gatewayUrl()); file.setContentSha256(hash); file.setSizeBytes(request.fileSizeBytes()); file.setPinStatus(stored.status()); ipfsFiles.save(file);
      version.setStorageStatus("STORED");
      audit.record("ASSET_VERSION_CREATED", actor.getUsername(), asset.getAssetCode(), "version=" + nextVersion + ";mode=" + stored.mode());
    } catch (RuntimeException error) {
      version.setStorageStatus("FAILED"); audit.record("ASSET_VERSION_STORE_FAILED", actor.getUsername(), asset.getAssetCode(), "version=" + nextVersion);
    }
    return versionResponse(asset, version);
  }

  @Transactional(readOnly = true)
  public FileHashVerificationResponse verifyUploadedFile(MultipartFile file, String expectedHash) {
    if (file == null || file.isEmpty()) throw new IllegalArgumentException("请选择非空文件");
    String computed = digest(file); String expected = normalizeHash(expectedHash);
    boolean matches = computed.equals(expected);
    AssetVersionEntity version = matches ? versions.findByContentSha256(expected).orElse(null) : null;
    AssetEntity asset = version == null ? null : assets.findById(version.getAssetId()).orElse(null);
    if (asset != null) assertCanManage(asset, currentUser.requireUser());
    return new FileHashVerificationResponse("sha256:" + computed, "sha256:" + expected, matches,
        asset == null ? null : asset.getAssetCode(), version == null ? null : version.getId(),
        "文件仅在请求内计算 SHA-256，不会由本接口保存；匹配结果不构成法律意义上的版权确权。");
  }

  @Transactional
  public ProvenanceReportResponse trace(String assetCode, String hash, String cid, Long transactionId) {
    AssetEntity asset = null;
    if (notBlank(assetCode)) asset = assetByCode(assetCode);
    if (notBlank(hash)) {
      String normalizedHash = normalizeHash(hash);
      AssetVersionEntity v = versions.findByContentSha256(normalizedHash).orElse(null);
      if (v != null) asset = sameAsset(asset, v.getAssetId());
      else asset = sameAsset(asset, assets.findByFileHash("sha256:" + normalizedHash).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("未找到该哈希资产")).getId());
    }
    if (notBlank(cid)) { IpfsFileEntity f = ipfsFiles.findByCid(cid).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("未找到该 CID")); AssetVersionEntity v = versions.findById(f.getAssetVersionId()).orElseThrow(); asset = sameAsset(asset, v.getAssetId()); }
    if (transactionId != null) {
      ChainTransactionEntity tx = chainTransactions.findById(transactionId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("未找到链交易"));
      if (tx.getAssetVersionId() != null) { AssetVersionEntity v = versions.findById(tx.getAssetVersionId()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("链交易缺少资产版本")); asset = sameAsset(asset, v.getAssetId()); }
      else asset = sameAsset(asset, assets.findByFileHash(tx.getFileHash()).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("链交易缺少资产信息")).getId());
    }
    if (asset == null) throw new IllegalArgumentException("至少提供 assetCode、hash、cid 或 transactionId 之一");
    assertCanManage(asset, currentUser.requireUser()); return report(asset);
  }

  @Transactional
  public ProvenanceReportResponse reportByAssetCode(String assetCode) { expireDueAuthorizations(); AssetEntity asset = assetByCode(assetCode); assertCanManage(asset, currentUser.requireUser()); return report(asset); }

  @Scheduled(cron = "0 5 0 * * *")
  @Transactional
  public void expireDueAuthorizations() {
    Instant now = Instant.now();
    for (AuthorizationEntity item : authorizations.findByStatus("ACTIVE")) {
      if (item.getEndsAt().isBefore(now)) {
        item.setStatus("EXPIRED"); AssetEntity asset = assets.findById(item.getAssetId()).orElse(null);
        if (asset != null) { recomputeAssetStatus(asset); audit.record("AUTHORIZATION_EXPIRED", "system", item.getAuthorizationNo(), "asset=" + asset.getAssetCode()); }
      }
    }
  }

  private void rejectDuplicate(Long assetId, AuthorizationCreateRequest request, String versionId) {
    for (AuthorizationEntity item : authorizations.findByAssetIdAndStatusIn(assetId, List.of("ACTIVE", "DRAFT"))) {
      boolean overlap = request.startsAt().isBefore(item.getEndsAt()) && request.endsAt().isAfter(item.getStartsAt());
      boolean sameScope = item.getLicenseeName().equalsIgnoreCase(request.licenseeName().trim())
          && item.getUsageType().equals(request.usageType()) && item.isCommercial() == request.commercial()
          && java.util.Objects.equals(item.getAssetVersionId(), versionId);
      if (overlap && sameScope) throw new IllegalStateException("存在同一被授权方、版本、用途和有效期重叠的授权记录");
    }
  }

  private ChainTransactionEntity authorizationTransaction(AssetEntity asset, AssetVersionEntity version, AuthorizationEntity authorization) {
    ChainTransactionEntity tx = new ChainTransactionEntity(); Instant now = Instant.now();
    tx.setTxId("pending_auth_" + java.util.UUID.randomUUID()); tx.setFileHash(asset.getFileHash()); tx.setAssetVersionId(version.getId());
    tx.setContractName(chainProperties.getContractName()); tx.setMethodName("recordAuthorization"); tx.setProvider("PENDING"); tx.setMode(chainProperties.getMode()); tx.setNetwork(chainProperties.getNetwork());
    tx.setIdempotencyKey("authorization-" + authorization.getId()); tx.setRequestDigest(sha256(authorization.getAuthorizationNo() + version.getContentSha256()));
    tx.setRequestPayload("{\"authorizationNo\":\"" + authorization.getAuthorizationNo() + "\",\"assetCode\":\"" + asset.getAssetCode() + "\"}"); tx.setResponsePayload("{}");
    tx.setStatus("PENDING"); tx.setAttemptNo(1); tx.setCreatedAt(now); tx.setUpdatedAt(now); return tx;
  }

  private void recomputeAssetStatus(AssetEntity asset) {
    Instant now = Instant.now(); List<AuthorizationEntity> all = authorizations.findByAssetIdOrderByCreatedAtDesc(asset.getId());
    if (all.stream().anyMatch(item -> "ACTIVE".equals(item.getStatus()) && !item.getStartsAt().isAfter(now) && item.getEndsAt().isAfter(now))) { asset.setStatus("AUTHORIZED"); return; }
    if (all.stream().anyMatch(item -> "REVOKED".equals(item.getStatus()))) { asset.setStatus("REVOKED"); return; }
    if (all.stream().anyMatch(item -> "EXPIRED".equals(item.getStatus()))) { asset.setStatus("EXPIRED"); return; }
    asset.setStatus("CERTIFIED");
  }

  private ProvenanceReportResponse report(AssetEntity asset) {
    List<TimelineEventResponse> events = new ArrayList<>();
    events.add(new TimelineEventResponse(asset.getCreatedAt(), "ASSET_CREATED", asset.getStatus(), asset.getAssetCode(), asset.getAssetName(), "DEMO"));
    for (AssetVersionEntity v : versions.findByAssetIdOrderByVersionNoDesc(asset.getId())) {
      events.add(new TimelineEventResponse(v.getCreatedAt(), "VERSION_CREATED", v.getStatus(), "v" + v.getVersionNo(), "sha256:" + v.getContentSha256(), null));
      for (IpfsFileEntity f : ipfsFiles.findByAssetVersionId(v.getId())) events.add(new TimelineEventResponse(f.getCreatedAt(), "STORAGE_RECORDED", f.getPinStatus(), f.getCid(), "sha256:" + f.getContentSha256(), f.getMode()));
      for (ChainTransactionEntity t : chainTransactions.findByAssetVersionIdOrderByCreatedAtAsc(v.getId())) events.add(new TimelineEventResponse(t.getCreatedAt(), "CHAIN_TRANSACTION", t.getStatus(), String.valueOf(t.getId()), t.getTxId(), t.getMode()));
    }
    evidenceRecords.findByAssetIdOrderByCreatedAtAsc(asset.getId()).forEach(e -> events.add(new TimelineEventResponse(e.getCreatedAt(), "EVIDENCE_RECORDED", "CONFIRMED", e.getTxId(), e.getFileHash(), "DEMO")));
    authorizations.findByAssetIdOrderByCreatedAtDesc(asset.getId()).forEach(a -> events.add(new TimelineEventResponse(a.getCreatedAt(), "AUTHORIZATION", a.getStatus(), a.getAuthorizationNo(), a.getLicenseeName(), a.getChainMode())));
    events.sort(Comparator.comparing(TimelineEventResponse::occurredAt));
    String mode = chainProperties.getMode();
    String disclaimer = "DEMO".equalsIgnoreCase(mode)
        ? "当前为 Demo/Mock 模式；链交易与存储标识不等同于真实链上记录，也不构成法律意义上的版权确权。"
        : "真实模式下仍需通过配置的链浏览器、合同与权利材料复核；系统记录不单独构成法律意义上的版权确权。";
    return new ProvenanceReportResponse(asset.getAssetCode(), asset.getStatus(), mode, disclaimer, events);
  }

  private AuthorizationResponse response(AuthorizationEntity a, AssetEntity asset) { return new AuthorizationResponse(a.getId(), a.getAuthorizationNo(), asset.getAssetCode(), a.getAssetVersionId(), a.getLicenseeName(), a.getLicenseeOrganization(), a.getUsageType(), a.isCommercial(), a.getRevenueShareRatio(), a.getStartsAt(), a.getEndsAt(), a.getStatus(), a.getChainTransactionId(), a.getChainTxId(), a.getChainMode(), a.getRevokedAt(), a.getRevokeReason(), "授权、撤销与到期均保留历史；Demo 链交易不构成法律意义上的版权确权。"); }
  private AssetVersionResponse versionResponse(AssetEntity asset, AssetVersionEntity v) { IpfsFileEntity f = ipfsFiles.findByAssetVersionId(v.getId()).stream().findFirst().orElse(null); String mode = f == null ? null : f.getMode(); return new AssetVersionResponse(v.getId(), asset.getAssetCode(), v.getVersionNo(), "sha256:" + v.getContentSha256(), f == null ? null : f.getCid(), mode, v.getStorageStatus(), v.getStatus(), v.getChangeNote(), "新版本保存历史哈希与存储标识；Demo CID 不是真实 IPFS CID。"); }
  private AssetEntity assetByCode(String code) { return assets.findByAssetCode(code).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("资产不存在")); }
  private AssetVersionEntity resolveVersion(AssetEntity asset, String id) { if (notBlank(id)) { AssetVersionEntity v = versions.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("资产版本不存在")); if (!asset.getId().equals(v.getAssetId())) throw new IllegalArgumentException("版本不属于该资产"); return v; } return versions.findByAssetIdOrderByVersionNoDesc(asset.getId()).stream().findFirst().orElseThrow(() -> new IllegalStateException("资产没有可授权版本")); }
  private AssetEntity sameAsset(AssetEntity current, Long id) { AssetEntity found = assets.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("资产不存在")); if (current != null && !current.getId().equals(found.getId())) throw new IllegalArgumentException("多个查询条件不属于同一资产"); return found; }
  private void assertCanManage(AssetEntity asset, UserAccountPrincipal actor) { if (actor.hasRole("SUPER_ADMIN")) return; if (actor.hasRole("MUSEUM_ADMIN") && actor.getOrganization() != null && actor.getOrganization().equals(asset.getOrganization())) return; if (actor.hasRole("CREATOR") && actor.getId().equals(asset.getOwnerUserId())) return; throw new AccessDeniedException("无权操作或查看该资产"); }
  private String normalizeHash(String value) { return value.replaceFirst("(?i)^sha256:", "").toLowerCase(Locale.ROOT); }
  private String digest(MultipartFile file) { try (InputStream input = file.getInputStream()) { MessageDigest digest = MessageDigest.getInstance("SHA-256"); byte[] buffer = new byte[8192]; for (int read; (read = input.read(buffer)) != -1;) digest.update(buffer, 0, read); return toHex(digest.digest()); } catch (IOException error) { throw new IllegalArgumentException("读取上传文件失败"); } catch (Exception error) { throw new IllegalStateException("SHA-256 不可用", error); } }
  private String sha256(String value) { try { return toHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); } catch (Exception error) { throw new IllegalStateException("SHA-256 不可用", error); } }
  private String toHex(byte[] bytes) { StringBuilder result = new StringBuilder(); for (byte value : bytes) result.append(String.format("%02x", value)); return result.toString(); }
  private String safeMessage(RuntimeException error) { String message = error.getMessage(); return message == null ? "链网关调用失败" : message.substring(0, Math.min(message.length(), 1000)); }
  private boolean notBlank(String value) { return value != null && !value.isBlank(); }
  private String blankToNull(String value) { return notBlank(value) ? value.trim() : null; }
}
