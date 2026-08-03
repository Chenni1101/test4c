package edu.suibe.evidence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "evidence.bootstrap")
public class BootstrapProperties {
  private boolean enabled;
  private String username;
  private String password;
  private String organization;

  public boolean isEnabled() { return enabled; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPassword() { return password; }
  public void setPassword(String password) { this.password = password; }
  public String getOrganization() { return organization; }
  public void setOrganization(String organization) { this.organization = organization; }
}
