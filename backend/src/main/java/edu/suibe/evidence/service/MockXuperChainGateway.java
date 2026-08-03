package edu.suibe.evidence.service;

import edu.suibe.evidence.config.ChainProperties;
import edu.suibe.evidence.dto.EvidenceCreateRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "evidence.chain.mode", havingValue = "DEMO", matchIfMissing = true)
public class MockXuperChainGateway implements BlockchainGateway {
  private final ChainProperties properties;

  public MockXuperChainGateway(ChainProperties properties) {
    this.properties = properties;
  }

  @Override
  public ChainReceipt saveEvidence(EvidenceCreateRequest request) {
    String seed = request.fileHash() + request.ownerName() + request.timestampIso();
    String txId = "xuper_" + sha256(seed).substring(0, 32);
    long blockHeight = 5_200_000L + Math.abs(seed.hashCode() % 900_000);
    String payload = """
        {"network":"%s","contract":"%s","method":"save","status":"CONFIRMED","time":"%s"}
        """.formatted(properties.getNetwork(), properties.getContractName(), Instant.now());
    return new ChainReceipt(txId, blockHeight, payload.trim(), "DEMO_XUPERCHAIN", "DEMO", "CONFIRMED");
  }

  @Override
  public ChainReceipt issueAsset(ChainAssetIssueRequest request) {
    String seed = request.assetCode() + request.contentSha256() + request.cid() + request.timestampIso();
    String txId = "demo_tx_" + sha256(seed).substring(0, 32);
    long blockHeight = 5_200_000L + Math.abs(seed.hashCode() % 900_000);
    String payload =
        """
        {"provider":"DEMO_XUPERCHAIN","mode":"DEMO","network":"%s","contract":"%s","method":"issueAsset","status":"CONFIRMED","time":"%s"}
        """.formatted(properties.getNetwork(), properties.getContractName(), Instant.now());
    return new ChainReceipt(txId, blockHeight, payload.trim(), "DEMO_XUPERCHAIN", "DEMO", "CONFIRMED");
  }

  @Override
  public ChainReceipt recordAuthorization(ChainAuthorizationRequest request) {
    String seed = request.authorizationNo() + request.assetCode() + request.licenseeName() + request.startsAt() + request.endsAt();
    String txId = "demo_auth_tx_" + sha256(seed).substring(0, 28);
    long blockHeight = 5_200_000L + Math.abs(seed.hashCode() % 900_000);
    String payload = """
        {"provider":"DEMO_XUPERCHAIN","mode":"DEMO","network":"%s","contract":"%s","method":"recordAuthorization","status":"CONFIRMED","time":"%s"}
        """.formatted(properties.getNetwork(), properties.getContractName(), Instant.now());
    return new ChainReceipt(txId, blockHeight, payload.trim(), "DEMO_XUPERCHAIN", "DEMO", "CONFIRMED");
  }

  private String sha256(String text) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] encoded = digest.digest(text.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      for (byte item : encoded) {
        builder.append(String.format("%02x", item));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException error) {
      throw new IllegalStateException("SHA-256 is not available", error);
    }
  }
}
