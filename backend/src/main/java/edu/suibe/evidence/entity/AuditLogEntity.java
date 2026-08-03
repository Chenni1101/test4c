package edu.suibe.evidence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, length = 64)
  private String action;

  @Column(nullable = false, length = 128)
  private String operatorName;

  private String targetHash;

  @Column(columnDefinition = "TEXT")
  private String detail;

  @Column(nullable = false)
  private Instant createdAt;

  public void setAction(String action) {
    this.action = action;
  }

  public void setOperatorName(String operatorName) {
    this.operatorName = operatorName;
  }

  public void setTargetHash(String targetHash) {
    this.targetHash = targetHash;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public String getAction() {
    return action;
  }
}
