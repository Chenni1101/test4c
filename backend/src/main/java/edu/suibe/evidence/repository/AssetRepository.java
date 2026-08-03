package edu.suibe.evidence.repository;

import edu.suibe.evidence.entity.AssetEntity;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<AssetEntity, Long> {
  Optional<AssetEntity> findByFileHash(String fileHash);

  Optional<AssetEntity> findByAssetCode(String assetCode);

  List<AssetEntity> findByOwnerUserId(Long ownerUserId);

  List<AssetEntity> findByOrganization(String organization);

  boolean existsByAssetCode(String assetCode);
}
