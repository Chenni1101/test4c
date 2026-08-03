package edu.suibe.evidence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "asset_versions")
public class AssetVersionEntity {
  @Id private String id;

  @Column(nullable = false)
  private Long assetId;

  @Column(nullable = false)
  private Integer versionNo;

  @Column(nullable = false, length = 64)
  private String contentSha256;

  @Column(nullable = false, length = 512)
  private String originalFilename;

  @Column(nullable = false, length = 127)
  private String mimeType;

  @Column(nullable = false)
  private Long fileSizeBytes;

  @Column(nullable = false, length = 32)
  private String storageStatus;

  @Column(nullable = false, length = 32)
  private String status;

  private Long submittedByUserId;

  @Column(length = 1000)
  private String changeNote;

  @Column(columnDefinition = "TEXT")
  private String metadataSnapshot;

  private Instant immutableAt;

  @Column(nullable = false)
  private Instant createdAt;

  @PrePersist
  void initialize() {
    if (id == null) id = UUID.randomUUID().toString();
    if (createdAt == null) createdAt = Instant.now();
  }

  public String getId() { return id; }
  public Long getAssetId() { return assetId; }
  public void setAssetId(Long assetId) { this.assetId = assetId; }
  public Integer getVersionNo() { return versionNo; }
  public void setVersionNo(Integer versionNo) { this.versionNo = versionNo; }
  public String getContentSha256() { return contentSha256; }
  public String getOriginalFilename() { return originalFilename; }
  public String getMimeType() { return mimeType; }
  public Long getFileSizeBytes() { return fileSizeBytes; }
  public String getStorageStatus() { return storageStatus; }
  public String getStatus() { return status; }
  public Long getSubmittedByUserId() { return submittedByUserId; }
  public String getChangeNote() { return changeNote; }
  public String getMetadataSnapshot() { return metadataSnapshot; }
  public Instant getImmutableAt() { return immutableAt; }
  public Instant getCreatedAt() { return createdAt; }
  public void setContentSha256(String contentSha256) { this.contentSha256 = contentSha256; }
  public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
  public void setMimeType(String mimeType) { this.mimeType = mimeType; }
  public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
  public void setStorageStatus(String storageStatus) { this.storageStatus = storageStatus; }
  public void setStatus(String status) { this.status = status; }
  public void setSubmittedByUserId(Long submittedByUserId) { this.submittedByUserId = submittedByUserId; }
  public void setChangeNote(String changeNote) { this.changeNote = changeNote; }
  public void setMetadataSnapshot(String metadataSnapshot) { this.metadataSnapshot = metadataSnapshot; }
  public void setImmutableAt(Instant immutableAt) { this.immutableAt = immutableAt; }
}
