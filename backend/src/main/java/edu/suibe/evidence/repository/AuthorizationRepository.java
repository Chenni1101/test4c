package edu.suibe.evidence.repository;

import edu.suibe.evidence.entity.AuthorizationEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationRepository extends JpaRepository<AuthorizationEntity, String> {
  List<AuthorizationEntity> findByAssetIdOrderByCreatedAtDesc(Long assetId);
  List<AuthorizationEntity> findByAssetIdAndStatusIn(Long assetId, Collection<String> statuses);
  List<AuthorizationEntity> findByStatus(String status);
  boolean existsByAuthorizationNo(String authorizationNo);
}
