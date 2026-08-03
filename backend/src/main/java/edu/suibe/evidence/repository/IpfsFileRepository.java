package edu.suibe.evidence.repository;

import edu.suibe.evidence.entity.IpfsFileEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpfsFileRepository extends JpaRepository<IpfsFileEntity, String> {
  List<IpfsFileEntity> findByAssetVersionId(String assetVersionId);

  Optional<IpfsFileEntity> findByCid(String cid);
}
