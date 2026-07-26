package edu.suibe.evidence.repository;

import edu.suibe.evidence.entity.ChainTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChainTransactionRepository extends JpaRepository<ChainTransactionEntity, Long> {}
