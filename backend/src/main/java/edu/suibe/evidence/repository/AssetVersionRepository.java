package edu.suibe.evidence.repository;

import edu.suibe.evidence.entity.AssetVersionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetVersionRepository extends JpaRepository<AssetVersionEntity, String> {
  List<AssetVersionEntity> findByAssetIdOrderByVersionNoDesc(Long assetId);

  java.util.Optional<AssetVersionEntity> findByContentSha256(String contentSha256);
  boolean existsByAssetIdAndContentSha256(Long assetId, String contentSha256);
}
