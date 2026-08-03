package edu.suibe.evidence.service;

import edu.suibe.evidence.config.ChainProperties;
import edu.suibe.evidence.config.EvidenceModeProperties;
import edu.suibe.evidence.dto.EvidenceCreateRequest;
import edu.suibe.evidence.dto.EvidenceResponse;
import edu.suibe.evidence.entity.AssetEntity;
import edu.suibe.evidence.entity.ChainTransactionEntity;
import edu.suibe.evidence.entity.EvidenceRecordEntity;
import edu.suibe.evidence.repository.AssetRepository;
import edu.suibe.evidence.repository.ChainTransactionRepository;
import edu.suibe.evidence.repository.EvidenceRecordRepository;
import edu.suibe.evidence.security.CurrentUserProvider;
import edu.suibe.evidence.security.UserAccountPrincipal;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 版权存证核心业务服务层
 * 实现存证创建、查询、列表展示等核心业务逻辑
 * 包含幂等校验、事务管理、多表联动、区块链交互等关键能力
 */
@Service
public class EvidenceService {
  // 资产信息表：存储数字资产元数据（名称、类型、创作者等）
  private final AssetRepository assetRepository;
  // 存证记录表：存储链上存证核心信息（哈希、交易ID、区块高度等）
  private final EvidenceRecordRepository evidenceRecordRepository;
  // 区块链交易表：存储所有链上交易的完整请求与响应
  private final ChainTransactionRepository chainTransactionRepository;
  // 区块链网关：封装与百度超级链的所有交互逻辑
  private final BlockchainGateway blockchainGateway;
  // 区块链配置：存储节点地址、合约名称、网络ID等配置信息
  private final ChainProperties chainProperties;
  private final EvidenceModeProperties evidenceModeProperties;
  private final CurrentUserProvider currentUserProvider;
  private final AuditService auditService;

  /**
   * 构造函数注入依赖（Spring推荐的依赖注入方式）
   */
  public EvidenceService(
      AssetRepository assetRepository,
      EvidenceRecordRepository evidenceRecordRepository,
      ChainTransactionRepository chainTransactionRepository,
      BlockchainGateway blockchainGateway,
      ChainProperties chainProperties,
      EvidenceModeProperties evidenceModeProperties,
      CurrentUserProvider currentUserProvider,
      AuditService auditService) {
    this.assetRepository = assetRepository;
    this.evidenceRecordRepository = evidenceRecordRepository;
    this.chainTransactionRepository = chainTransactionRepository;
    this.blockchainGateway = blockchainGateway;
    this.chainProperties = chainProperties;
    this.evidenceModeProperties = evidenceModeProperties;
    this.currentUserProvider = currentUserProvider;
    this.auditService = auditService;
  }

  /**
   * 【核心：幂等校验】创建版权存证
   * 先查询该文件哈希是否已存在存证记录
   * 存在则直接返回已有存证结果，避免重复上链
   * 不存在则执行完整的存证流程
   * @param request 存证请求DTO
   * @return 存证响应DTO
   */
  @Transactional(rollbackFor = Exception.class)
  public EvidenceResponse createEvidence(EvidenceCreateRequest request) {
    return evidenceRecordRepository
        .findByFileHash(request.fileHash())
        .map(
            evidence -> {
              AssetEntity asset = findAsset(evidence);
              assertCanRead(asset, currentUserProvider.requireUser());
              return toResponse(evidence, asset);
            })
        .orElseGet(() -> createNewEvidence(request));
  }

  /**
   * 根据文件哈希查询存证记录
   * 对应前端查询页的"按哈希查询"功能
   * @param hash 文件SHA-256哈希
   * @return 存证响应DTO
   */
  @Transactional(readOnly = true)
  public EvidenceResponse getEvidenceByHash(String hash) {
    EvidenceRecordEntity evidence =
        evidenceRecordRepository
            .findByFileHash(hash)
            .orElseThrow(() -> new EntityNotFoundException("未找到该哈希对应的存证记录"));
    AssetEntity asset = findAsset(evidence);
    UserAccountPrincipal actor = currentUserProvider.requireUser();
    assertCanRead(asset, actor);
    auditService.record("READ_EVIDENCE", actor.getUsername(), hash, "asset=" + asset.getAssetName());
    return toResponse(evidence, asset);
  }

  /**
   * 查询所有存证记录列表
   * 对应前端资产管理页的资产列表功能
   * @return 存证响应DTO列表
   */
  @Transactional(readOnly = true)
  public List<EvidenceResponse> listEvidence() {
    UserAccountPrincipal actor = currentUserProvider.requireUser();
    return evidenceRecordRepository.findAll().stream()
        .map(evidence -> new EvidenceWithAsset(evidence, findAsset(evidence)))
        .filter(item -> canRead(item.asset(), actor))
        .map(item -> toResponse(item.evidence(), item.asset()))
        .toList();
  }

  /**
   * 执行完整的新存证流程
   * 采用事务管理，任何一步失败都会回滚所有操作
   * 实现"链上存关键证据、链下存业务数据"的核心设计
   */
  private EvidenceResponse createNewEvidence(EvidenceCreateRequest request) {
    Instant now = Instant.now();
    UserAccountPrincipal actor = currentUserProvider.requireUser();
    assertCanCreate(actor);

    // 1. 保存链下业务数据：数字资产元信息
    AssetEntity asset = new AssetEntity();
    asset.setAssetName(request.assetName());
    asset.setAssetType(request.assetType());
    asset.setCreator(request.creator());
    asset.setOrganization(resolveOrganization(request.organization(), actor));
    asset.setOwnerUserId(actor.getId());
    asset.setDescription(request.description());
    asset.setFileHash(request.fileHash());
    asset.setCid(request.cid());
    asset.setStatus("CERTIFIED");
    asset.setCreatedAt(now);
    AssetEntity savedAsset = assetRepository.save(asset);

    // 2. 调用区块链网关，将关键证据上链存证
    // 上链内容：文件哈希、CID、时间戳、权属信息（仅约1KB数据）
    String authenticatedOwner = actor.getUsername();
    EvidenceCreateRequest chainRequest =
        new EvidenceCreateRequest(
            request.assetName(),
            request.assetType(),
            request.creator(),
            savedAsset.getOrganization(),
            request.description(),
            request.keywords(),
            request.fileHash(),
            request.cid(),
            authenticatedOwner,
            request.timestampIso());
    ChainReceipt receipt = blockchainGateway.saveEvidence(chainRequest);

    // 3. 保存链上存证记录：关联资产ID与链上交易信息
    EvidenceRecordEntity evidence = new EvidenceRecordEntity();
    evidence.setAssetId(savedAsset.getId());
    evidence.setFileHash(request.fileHash());
    evidence.setOwnerName(authenticatedOwner);
    evidence.setTimestampIso(request.timestampIso());
    evidence.setChainNetwork(chainProperties.getNetwork());
    evidence.setContractName(chainProperties.getContractName());
    evidence.setTxId(receipt.txId());
    evidence.setBlockHeight(receipt.blockHeight());
    evidence.setCreatedAt(now);
    EvidenceRecordEntity savedEvidence = evidenceRecordRepository.save(evidence);

    // 4. 保存完整区块链交易记录：用于后续审计和问题排查
    ChainTransactionEntity transaction = new ChainTransactionEntity();
    transaction.setTxId(receipt.txId());
    transaction.setFileHash(request.fileHash());
    transaction.setContractName(chainProperties.getContractName());
    transaction.setMethodName("save");
    transaction.setRequestPayload(chainRequest.toString());
    transaction.setResponsePayload(receipt.responsePayload());
    transaction.setStatus("CONFIRMED");
    transaction.setCreatedAt(now);
    chainTransactionRepository.save(transaction);

    // 5. 写入审计日志：记录所有关键操作，全程可追溯
    auditService.record("CREATE_EVIDENCE", actor.getUsername(), request.fileHash(), "asset=" + request.assetName());

    return toResponse(savedEvidence, savedAsset);
  }

  /**
   * 实体类转响应DTO
   * 封装数据库实体到前端展示数据的转换逻辑
   */
  private EvidenceResponse toResponse(EvidenceRecordEntity evidence, AssetEntity asset) {
    return new EvidenceResponse(
        asset.getAssetName(),
        asset.getAssetType(),
        asset.getCreator(),
        asset.getOrganization(),
        asset.getFileHash(),
        asset.getCid(),
        "H_" + asset.getFileHash().replace("sha256:", "").substring(0, 16),
        evidence.getTxId(),
        evidence.getBlockHeight(),
        evidence.getChainNetwork(),
        evidence.getContractName(),
        evidence.getTimestampIso(),
        asset.getStatus(),
        evidenceModeProperties.getMode());
  }

  private AssetEntity findAsset(EvidenceRecordEntity evidence) {
    return assetRepository
        .findByFileHash(evidence.getFileHash())
        .orElseThrow(() -> new EntityNotFoundException("存证记录缺少资产信息"));
  }

  private void assertCanCreate(UserAccountPrincipal actor) {
    if (actor.hasRole("SUPER_ADMIN") || actor.hasRole("MUSEUM_ADMIN") || actor.hasRole("CREATOR")) return;
    throw new org.springframework.security.access.AccessDeniedException("当前角色不能提交存证");
  }

  private void assertCanRead(AssetEntity asset, UserAccountPrincipal actor) {
    if (!canRead(asset, actor)) {
      throw new org.springframework.security.access.AccessDeniedException("无权访问该资产");
    }
  }

  private boolean canRead(AssetEntity asset, UserAccountPrincipal actor) {
    if (actor.hasRole("SUPER_ADMIN")) return true;
    if (actor.hasRole("MUSEUM_ADMIN")) {
      return actor.getOrganization() != null && actor.getOrganization().equals(asset.getOrganization());
    }
    return actor.hasRole("CREATOR") && actor.getId().equals(asset.getOwnerUserId());
  }

  private String resolveOrganization(String requestedOrganization, UserAccountPrincipal actor) {
    if (actor.hasRole("MUSEUM_ADMIN")) {
      if (actor.getOrganization() == null || actor.getOrganization().isBlank()) {
        throw new IllegalStateException("文博管理员必须关联所属机构");
      }
      return actor.getOrganization();
    }
    if (actor.hasRole("CREATOR") && actor.getOrganization() != null && !actor.getOrganization().isBlank()) {
      return actor.getOrganization();
    }
    return requestedOrganization;
  }

  private record EvidenceWithAsset(EvidenceRecordEntity evidence, AssetEntity asset) {}
}
