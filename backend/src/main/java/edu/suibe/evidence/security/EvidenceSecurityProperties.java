package edu.suibe.evidence.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "evidence.security")
public class EvidenceSecurityProperties {
  private List<String> corsAllowedOrigins = List.of("http://localhost:5173");

  public List<String> getCorsAllowedOrigins() {
    return corsAllowedOrigins;
  }

  public void setCorsAllowedOrigins(List<String> corsAllowedOrigins) {
    this.corsAllowedOrigins = corsAllowedOrigins;
  }
}
