package edu.suibe.evidence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "chain_transactions")
public class ChainTransactionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, unique = true, length = 128)
  private String txId;

  @Column(nullable = false, length = 128)
  private String fileHash;

  @Column(nullable = false, length = 64)
  private String contractName;

  @Column(nullable = false, length = 64)
  private String methodName;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String requestPayload;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String responsePayload;

  @Column(nullable = false, length = 32)
  private String status;

  @Column(nullable = false)
  private Instant createdAt;

  private String assetVersionId;
  private String provider;
  private String mode;
  private String network;
  private String idempotencyKey;
  private String requestDigest;
  private String errorCode;
  private String errorMessage;
  private Integer attemptNo;
  private Instant confirmedAt;
  private Long chainBlockHeight;
  private Instant updatedAt;

  public Long getId() { return id; }
  public String getAssetVersionId() { return assetVersionId; }
  public String getProvider() { return provider; }
  public String getMode() { return mode; }
  public String getNetwork() { return network; }
  public String getIdempotencyKey() { return idempotencyKey; }
  public String getErrorCode() { return errorCode; }
  public String getErrorMessage() { return errorMessage; }
  public Integer getAttemptNo() { return attemptNo; }
  public Instant getConfirmedAt() { return confirmedAt; }
  public Long getChainBlockHeight() { return chainBlockHeight; }
  public Instant getUpdatedAt() { return updatedAt; }

  public String getTxId() {
    return txId;
  }

  public void setTxId(String txId) {
    this.txId = txId;
  }

  public String getFileHash() {
    return fileHash;
  }

  public void setFileHash(String fileHash) {
    this.fileHash = fileHash;
  }

  public String getContractName() {
    return contractName;
  }

  public void setContractName(String contractName) {
    this.contractName = contractName;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public String getRequestPayload() {
    return requestPayload;
  }

  public void setRequestPayload(String requestPayload) {
    this.requestPayload = requestPayload;
  }

  public String getResponsePayload() {
    return responsePayload;
  }

  public void setResponsePayload(String responsePayload) {
    this.responsePayload = responsePayload;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public void setAssetVersionId(String assetVersionId) { this.assetVersionId = assetVersionId; }
  public void setProvider(String provider) { this.provider = provider; }
  public void setMode(String mode) { this.mode = mode; }
  public void setNetwork(String network) { this.network = network; }
  public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
  public void setRequestDigest(String requestDigest) { this.requestDigest = requestDigest; }
  public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
  public void setAttemptNo(Integer attemptNo) { this.attemptNo = attemptNo; }
  public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }
  public void setChainBlockHeight(Long chainBlockHeight) { this.chainBlockHeight = chainBlockHeight; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
