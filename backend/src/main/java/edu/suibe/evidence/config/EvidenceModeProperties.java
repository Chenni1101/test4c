package edu.suibe.evidence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Platform integration mode. The current implementation only supports the clearly labeled DEMO mode. */
@Configuration
@ConfigurationProperties(prefix = "evidence")
public class EvidenceModeProperties {
  private String mode = "DEMO";

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }
}
