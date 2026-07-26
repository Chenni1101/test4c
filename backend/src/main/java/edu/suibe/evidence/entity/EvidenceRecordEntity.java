package edu.suibe.evidence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "evidence_records")
public class EvidenceRecordEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false)
  private Long assetId;

  @Column(nullable = false, unique = true, length = 128)
  private String fileHash;

  @Column(nullable = false, length = 128)
  private String ownerName;

  @Column(nullable = false, length = 64)
  private String timestampIso;

  @Column(nullable = false, length = 128)
  private String chainNetwork;

  @Column(nullable = false, length = 64)
  private String contractName;

  @Column(nullable = false, unique = true, length = 128)
  private String txId;

  @Column(nullable = false)
  private Long blockHeight;

  @Column(nullable = false)
  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getAssetId() {
    return assetId;
  }

  public void setAssetId(Long assetId) {
    this.assetId = assetId;
  }

  public String getFileHash() {
    return fileHash;
  }

  public void setFileHash(String fileHash) {
    this.fileHash = fileHash;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public String getTimestampIso() {
    return timestampIso;
  }

  public void setTimestampIso(String timestampIso) {
    this.timestampIso = timestampIso;
  }

  public String getChainNetwork() {
    return chainNetwork;
  }

  public void setChainNetwork(String chainNetwork) {
    this.chainNetwork = chainNetwork;
  }

  public String getContractName() {
    return contractName;
  }

  public void setContractName(String contractName) {
    this.contractName = contractName;
  }

  public String getTxId() {
    return txId;
  }

  public void setTxId(String txId) {
    this.txId = txId;
  }

  public Long getBlockHeight() {
    return blockHeight;
  }

  public void setBlockHeight(Long blockHeight) {
    this.blockHeight = blockHeight;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
