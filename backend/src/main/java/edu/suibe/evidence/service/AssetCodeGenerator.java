package edu.suibe.evidence.service;

import edu.suibe.evidence.repository.AssetRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AssetCodeGenerator {
  private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
  private final SecureRandom random = new SecureRandom();
  private final AssetRepository assetRepository;

  public AssetCodeGenerator(AssetRepository assetRepository) { this.assetRepository = assetRepository; }

  public String nextCode() {
    for (int attempt = 0; attempt < 10; attempt++) {
      StringBuilder suffix = new StringBuilder(6);
      for (int index = 0; index < 6; index++) suffix.append(ALPHABET[random.nextInt(ALPHABET.length)]);
      String code = "AST-" + LocalDate.now().toString().replace("-", "") + "-" + suffix;
      if (!assetRepository.existsByAssetCode(code)) return code;
    }
    throw new IllegalStateException("无法生成唯一资产编号");
  }
}
