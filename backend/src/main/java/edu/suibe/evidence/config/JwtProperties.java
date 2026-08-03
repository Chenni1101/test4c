package edu.suibe.evidence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "evidence.jwt")
public class JwtProperties {
  private String secret;
  private long expirationMinutes = 120;

  public String getSecret() { return secret; }
  public void setSecret(String secret) { this.secret = secret; }
  public long getExpirationMinutes() { return expirationMinutes; }
  public void setExpirationMinutes(long expirationMinutes) { this.expirationMinutes = expirationMinutes; }
}
