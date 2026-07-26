package edu.suibe.evidence.service;

import edu.suibe.evidence.config.ChainProperties;
import edu.suibe.evidence.dto.EvidenceCreateRequest;
import edu.suibe.evidence.dto.EvidenceResponse;
import edu.suibe.evidence.entity.AssetEntity;
import edu.suibe.evidence.entity.AuditLogEntity;
import edu.suibe.evidence.entity.ChainTransactionEntity;
import edu.suibe.evidence.entity.EvidenceRecordEntity;
import edu.suibe.evidence.repository.AssetRepository;
import edu.suibe.evidence.repository.AuditLogRepository;
import edu.suibe.evidence.repository.ChainTransactionRepository;
import edu.suibe.evidence.repository.EvidenceRecordRepository;
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
  // 审计日志表：记录所有关键操作，用于追溯和合规审计
  private final AuditLogRepository auditLogRepository;
  // 区块链网关：封装与百度超级链的所有交互逻辑
  private final BlockchainGateway blockchainGateway;
  // 区块链配置：存储节点地址、合约名称、网络ID等配置信息
  private final ChainProperties chainProperties;

  /**
   * 构造函数注入依赖（Spring推荐的依赖注入方式）
   */
  public EvidenceService(
      AssetRepository assetRepository,
      EvidenceRecordRepository evidenceRecordRepository,
      ChainTransactionRepository chainTransactionRepository,
      AuditLogRepository auditLogRepository,
      BlockchainGateway blockchainGateway,
      ChainProperties chainProperties) {
    this.assetRepository = assetRepository;
    this.evidenceRecordRepository = evidenceRecordRepository;
    this.chainTransactionRepository = chainTransactionRepository;
    this.auditLogRepository = auditLogRepository;
    this.blockchainGateway = blockchainGateway;
    this.chainProperties = chainProperties;
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
        .map(this::toResponse)
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
    return evidenceRecordRepository
        .findByFileHash(hash)
        .map(this::toResponse)
        .orElseThrow(() -> new EntityNotFoundException("未找到该哈希对应的存证记录"));
  }

  /**
   * 查询所有存证记录列表
   * 对应前端资产管理页的资产列表功能
   * @return 存证响应DTO列表
   */
  @Transactional(readOnly = true)
  public List<EvidenceResponse> listEvidence() {
    return evidenceRecordRepository.findAll().stream().map(this::toResponse).toList();
  }

  /**
   * 执行完整的新存证流程
   * 采用事务管理，任何一步失败都会回滚所有操作
   * 实现"链上存关键证据、链下存业务数据"的核心设计
   */
  private EvidenceResponse createNewEvidence(EvidenceCreateRequest request) {
    Instant now = Instant.now();

    // 1. 保存链下业务数据：数字资产元信息
    AssetEntity asset = new AssetEntity();
    asset.setAssetName(request.assetName());
    asset.setAssetType(request.assetType());
    asset.setCreator(request.creator());
    asset.setOrganization(request.organization());
    asset.setDescription(request.description());
    asset.setFileHash(request.fileHash());
    asset.setCid(request.cid());
    asset.setStatus("CERTIFIED");
    asset.setCreatedAt(now);
    AssetEntity savedAsset = assetRepository.save(asset);

    // 2. 调用区块链网关，将关键证据上链存证
    // 上链内容：文件哈希、CID、时间戳、权属信息（仅约1KB数据）
    ChainReceipt receipt = blockchainGateway.saveEvidence(request);

    // 3. 保存链上存证记录：关联资产ID与链上交易信息
    EvidenceRecordEntity evidence = new EvidenceRecordEntity();
    evidence.setAssetId(savedAsset.getId());
    evidence.setFileHash(request.fileHash());
    evidence.setOwnerName(request.ownerName());
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
    transaction.setRequestPayload(request.toString());
    transaction.setResponsePayload(receipt.responsePayload());
    transaction.setStatus("CONFIRMED");
    transaction.setCreatedAt(now);
    chainTransactionRepository.save(transaction);

    // 5. 写入审计日志：记录所有关键操作，全程可追溯
    writeAudit("CREATE_EVIDENCE", request.ownerName(), request.fileHash(), "asset=" + request.assetName());

    return toResponse(savedEvidence);
  }

  /**
   * 实体类转响应DTO
   * 封装数据库实体到前端展示数据的转换逻辑
   */
  private EvidenceResponse toResponse(EvidenceRecordEntity evidence) {
    AssetEntity asset =
        assetRepository
            .findByFileHash(evidence.getFileHash())
            .orElseThrow(() -> new EntityNotFoundException("存证记录缺少资产信息"));
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
        asset.getStatus());
  }

  /**
   * 写入审计日志
   * 记录操作类型、操作人、目标哈希和详细信息
   */
  private void writeAudit(String action, String operatorName, String targetHash, String detail) {
    AuditLogEntity auditLog = new AuditLogEntity();
    auditLog.setAction(action);
    auditLog.setOperatorName(operatorName);
    auditLog.setTargetHash(targetHash);
    auditLog.setDetail(detail);
    auditLog.setCreatedAt(Instant.now());
    auditLogRepository.save(auditLog);
  }
}