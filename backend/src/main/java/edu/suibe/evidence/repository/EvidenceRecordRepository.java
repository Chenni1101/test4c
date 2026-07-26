package edu.suibe.evidence.repository;

import edu.suibe.evidence.entity.EvidenceRecordEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRecordRepository extends JpaRepository<EvidenceRecordEntity, Long> {
  Optional<EvidenceRecordEntity> findByFileHash(String fileHash);
}
