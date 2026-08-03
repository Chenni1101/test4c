package edu.suibe.evidence.security;

import edu.suibe.evidence.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtProperties properties;
  private final SecretKey key;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    byte[] secret = properties.getSecret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < 32) throw new IllegalStateException("JWT 密钥必须至少包含 32 个字节");
    this.key = Keys.hmacShaKeyFor(secret);
  }

  public String generate(UserAccountPrincipal principal) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(principal.getUsername())
        .claim("uid", principal.getId())
        .claim("roles", principal.roleCodes())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(properties.getExpirationMinutes() * 60)))
        .signWith(key)
        .compact();
  }

  public String extractUsername(String token) {
    return claims(token).getSubject();
  }

  private Claims claims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
