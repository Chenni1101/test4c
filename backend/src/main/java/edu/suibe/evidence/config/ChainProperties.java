package edu.suibe.evidence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "evidence.chain")
public class ChainProperties {
  private String network;
  private String contractName;
  private String nodeEndpoint;

  public String getNetwork() {
    return network;
  }

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
