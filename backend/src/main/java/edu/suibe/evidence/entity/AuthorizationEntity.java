package edu.suibe.evidence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "authorizations")
public class AuthorizationEntity {
  @Id private String id;
  @Column(nullable = false, unique = true, length = 64) private String authorizationNo;
  @Column(nullable = false) private Long assetId;
  private String assetVersionId;
  @Column(nullable = false) private Long licensorUserId;
  @Column(nullable = false, length = 255) private String licenseeName;
  @Column(length = 255) private String licenseeOrganization;
  @Column(nullable = false, length = 32) private String usageType;
  @Column(nullable = false) private boolean commercial;
  @Column(nullable = false, precision = 5, scale = 2) private BigDecimal revenueShareRatio;
  @Column(nullable = false) private Instant startsAt;
  @Column(nullable = false) private Instant endsAt;
  @Column(nullable = false, length = 32) private String status;
  private Long chainTransactionId;
  @Column(length = 128) private String chainTxId;
  @Column(length = 16) private String chainMode;
  private Instant revokedAt;
  @Column(length = 1000) private String revokeReason;
  @Column(nullable = false) private Instant createdAt;
  @Column(nullable = false) private Instant updatedAt;

  @PrePersist void create() { if (id == null) id = UUID.randomUUID().toString(); Instant now = Instant.now(); if (createdAt == null) createdAt = now; if (updatedAt == null) updatedAt = now; }
  @PreUpdate void update() { updatedAt = Instant.now(); }
  public String getId() { return id; }
  public String getAuthorizationNo() { return authorizationNo; }
  public Long getAssetId() { return assetId; }
  public String getAssetVersionId() { return assetVersionId; }
  public Long getLicensorUserId() { return licensorUserId; }
  public String getLicenseeName() { return licenseeName; }
  public String getLicenseeOrganization() { return licenseeOrganization; }
  public String getUsageType() { return usageType; }
  public boolean isCommercial() { return commercial; }
  public BigDecimal getRevenueShareRatio() { return revenueShareRatio; }
  public Instant getStartsAt() { return startsAt; }
  public Instant getEndsAt() { return endsAt; }
  public String getStatus() { return status; }
  public Long getChainTransactionId() { return chainTransactionId; }
  public String getChainTxId() { return chainTxId; }
  public String getChainMode() { return chainMode; }
  public Instant getRevokedAt() { return revokedAt; }
  public String getRevokeReason() { return revokeReason; }
  public Instant getCreatedAt() { return createdAt; }
  public void setAuthorizationNo(String value) { authorizationNo = value; }
  public void setAssetId(Long value) { assetId = value; }
  public void setAssetVersionId(String value) { assetVersionId = value; }
  public void setLicensorUserId(Long value) { licensorUserId = value; }
  public void setLicenseeName(String value) { licenseeName = value; }
  public void setLicenseeOrganization(String value) { licenseeOrganization = value; }
  public void setUsageType(String value) { usageType = value; }
  public void setCommercial(boolean value) { commercial = value; }
  public void setRevenueShareRatio(BigDecimal value) { revenueShareRatio = value; }
  public void setStartsAt(Instant value) { startsAt = value; }
  public void setEndsAt(Instant value) { endsAt = value; }
  public void setStatus(String value) { status = value; }
  public void setChainTransactionId(Long value) { chainTransactionId = value; }
  public void setChainTxId(String value) { chainTxId = value; }
  public void setChainMode(String value) { chainMode = value; }
  public void setRevokedAt(Instant value) { revokedAt = value; }
  public void setRevokeReason(String value) { revokeReason = value; }
}
