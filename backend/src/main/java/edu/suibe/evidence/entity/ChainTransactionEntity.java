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
}
