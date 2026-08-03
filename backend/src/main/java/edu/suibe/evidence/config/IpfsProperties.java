package edu.suibe.evidence.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "evidence.ipfs")
public class IpfsProperties {
  private String mode = "DEMO";
  private List<String> providerOrder = List.of("DEMO_LOCAL");

  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }
  public List<String> getProviderOrder() { return providerOrder; }
  public void setProviderOrder(List<String> providerOrder) { this.providerOrder = providerOrder; }
}
