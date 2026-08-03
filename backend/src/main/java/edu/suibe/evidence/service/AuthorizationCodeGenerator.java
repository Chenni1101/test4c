package edu.suibe.evidence.service;

import edu.suibe.evidence.repository.AuthorizationRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationCodeGenerator {
  private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
  private final SecureRandom random = new SecureRandom();
  private final AuthorizationRepository repository;
  public AuthorizationCodeGenerator(AuthorizationRepository repository) { this.repository = repository; }
  public String nextCode() {
    for (int attempt = 0; attempt < 20; attempt++) {
      StringBuilder suffix = new StringBuilder();
      for (int index = 0; index < 6; index++) suffix.append(ALPHABET[random.nextInt(ALPHABET.length)]);
      String code = "AUTH-" + LocalDate.now().toString().replace("-", "") + "-" + suffix;
      if (!repository.existsByAuthorizationNo(code)) return code;
    }
    throw new IllegalStateException("无法生成唯一授权编号");
  }
}
