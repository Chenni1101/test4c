package edu.suibe.evidence.repository;

import edu.suibe.evidence.entity.ChainTransactionEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChainTransactionRepository extends JpaRepository<ChainTransactionEntity, Long> {
  Optional<ChainTransactionEntity> findByIdempotencyKey(String idempotencyKey);
  List<ChainTransactionEntity> findByAssetVersionIdOrderByCreatedAtAsc(String assetVersionId);
}
