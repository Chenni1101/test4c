package edu.suibe.evidence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "evidence.chain")
public class ChainProperties {
  private String mode = "DEMO";
  private String network;
  private String contractName;
  private String nodeEndpoint;

  public String getNetwork() {
    return network;
  }

  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }

  public void setNetwork(String network) {
    this.network = network;
  }

  public String getContractName() {
    return contractName;
  }

  public void setContractName(String contractName) {
    this.contractName = contractName;
  }

  public String getNodeEndpoint() {
    return nodeEndpoint;
  }

  public void setNodeEndpoint(String nodeEndpoint) {
    this.nodeEndpoint = nodeEndpoint;
  }
}
