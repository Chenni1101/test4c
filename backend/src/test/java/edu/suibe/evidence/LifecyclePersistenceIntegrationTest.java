package edu.suibe.evidence;

import static org.assertj.core.api.Assertions.assertThat;

import edu.suibe.evidence.entity.AssetEntity;
import edu.suibe.evidence.entity.AssetVersionEntity;
import edu.suibe.evidence.entity.IpfsFileEntity;
import edu.suibe.evidence.entity.RoleEntity;
import edu.suibe.evidence.repository.AssetRepository;
import edu.suibe.evidence.repository.AssetVersionRepository;
import edu.suibe.evidence.repository.IpfsFileRepository;
import edu.suibe.evidence.repository.RoleRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class LifecyclePersistenceIntegrationTest {
  @Autowired private AssetRepository assetRepository;
  @Autowired private AssetVersionRepository assetVersionRepository;
  @Autowired private IpfsFileRepository ipfsFileRepository;
  @Autowired private RoleRepository roleRepository;

  @Test
  @Transactional
  void lifecycleFoundationPersistsRoleVersionAndClearlyLabeledDemoStorage() {
    RoleEntity curator = new RoleEntity();
    curator.setCode("CURATOR_TEST");
    curator.setName("测试馆员");
    curator.setSystem(false);
    roleRepository.saveAndFlush(curator);

    AssetEntity asset = new AssetEntity();
    asset.setAssetName("迁移验证资产");
    asset.setAssetType("image");
    asset.setCreator("测试团队");
    asset.setFileHash("sha256:" + "b".repeat(64));
    asset.setStatus("DRAFT");
    asset.setCreatedAt(Instant.now());
    assetRepository.saveAndFlush(asset);

    AssetVersionEntity version = new AssetVersionEntity();
    version.setAssetId(asset.getId());
    version.setVersionNo(1);
    version.setContentSha256("b".repeat(64));
    version.setOriginalFilename("asset.png");
    version.setMimeType("image/png");
    version.setFileSizeBytes(1024L);
    version.setStorageStatus("STORED");
    version.setStatus("DRAFT");
    assetVersionRepository.saveAndFlush(version);

    IpfsFileEntity ipfsFile = new IpfsFileEntity();
    ipfsFile.setAssetVersionId(version.getId());
    ipfsFile.setProvider("DEMO");
    ipfsFile.setMode("DEMO");
    ipfsFile.setCid("demo-cid-lifecycle-test");
    ipfsFile.setContentSha256("b".repeat(64));
    ipfsFile.setSizeBytes(1024L);
    ipfsFile.setPinStatus("PINNED");
    ipfsFileRepository.saveAndFlush(ipfsFile);

    assertThat(curator.getId()).isNotBlank();
    assertThat(version.getId()).isNotBlank();
    assertThat(ipfsFileRepository.findByAssetVersionId(version.getId())).hasSize(1);
  }
}
