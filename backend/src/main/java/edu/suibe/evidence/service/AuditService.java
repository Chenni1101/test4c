package edu.suibe.evidence.service;

import edu.suibe.evidence.entity.AuditLogEntity;
import edu.suibe.evidence.repository.AuditLogRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditLogRepository auditLogRepository;

  public AuditService(AuditLogRepository auditLogRepository) { this.auditLogRepository = auditLogRepository; }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(String action, String actor, String target, String detail) {
    AuditLogEntity log = new AuditLogEntity();
    log.setAction(action);
    log.setOperatorName(actor == null || actor.isBlank() ? "anonymous" : actor);
    log.setTargetHash(target);
    log.setDetail(detail);
    log.setCreatedAt(Instant.now());
    auditLogRepository.save(log);
  }
}
