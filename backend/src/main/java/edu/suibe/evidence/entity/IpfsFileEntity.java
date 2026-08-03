package edu.suibe.evidence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ipfs_files")
public class IpfsFileEntity {
  @Id private String id;

  @Column(nullable = false)
  private String assetVersionId;

  @Column(nullable = false, length = 32)
  private String provider;

  @Column(nullable = false, length = 16)
  private String mode;

  @Column(length = 255)
  private String cid;

  @Column(length = 2048)
  private String gatewayUrl;

  @Column(nullable = false, length = 64)
  private String contentSha256;

  @Column(nullable = false)
  private Long sizeBytes;

  @Column(nullable = false, length = 32)
  private String pinStatus;

  @Column(length = 255)
  private String providerRequestId;

  @Column(length = 64)
  private String errorCode;

  @Column(length = 1000)
  private String errorMessage;

  private Instant pinnedAt;
  private Instant verifiedAt;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  void initialize() {
    if (id == null) id = UUID.randomUUID().toString();
    if (createdAt == null) createdAt = Instant.now();
  }

  public String getId() { return id; }
  public String getAssetVersionId() { return assetVersionId; }
  public String getProvider() { return provider; }
  public String getMode() { return mode; }
  public String getCid() { return cid; }
  public String getGatewayUrl() { return gatewayUrl; }
  public String getContentSha256() { return contentSha256; }
  public Long getSizeBytes() { return sizeBytes; }
  public String getPinStatus() { return pinStatus; }
  public String getErrorCode() { return errorCode; }
  public String getErrorMessage() { return errorMessage; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getPinnedAt() { return pinnedAt; }
  public void setAssetVersionId(String assetVersionId) { this.assetVersionId = assetVersionId; }
  public void setProvider(String provider) { this.provider = provider; }
  public void setMode(String mode) { this.mode = mode; }
  public void setCid(String cid) { this.cid = cid; }
  public void setGatewayUrl(String gatewayUrl) { this.gatewayUrl = gatewayUrl; }
  public void setContentSha256(String contentSha256) { this.contentSha256 = contentSha256; }
  public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
  public void setPinStatus(String pinStatus) { this.pinStatus = pinStatus; }
  public void setProviderRequestId(String providerRequestId) { this.providerRequestId = providerRequestId; }
  public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
  public void setPinnedAt(Instant pinnedAt) { this.pinnedAt = pinnedAt; }
  public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
}
