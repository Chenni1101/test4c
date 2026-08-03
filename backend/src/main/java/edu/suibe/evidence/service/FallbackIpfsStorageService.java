package edu.suibe.evidence.service;

import edu.suibe.evidence.config.IpfsProperties;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FallbackIpfsStorageService {
  private final IpfsProperties properties;
  private final List<IpfsStorageGateway> gateways;

  public FallbackIpfsStorageService(IpfsProperties properties, List<IpfsStorageGateway> gateways) {
    this.properties = properties;
    this.gateways = gateways;
  }

  public IpfsStoreResult store(IpfsStoreRequest request) {
    RuntimeException lastError = null;
    for (String provider : properties.getProviderOrder()) {
      for (IpfsStorageGateway gateway : gateways) {
        if (!gateway.provider().equals(provider) || !gateway.supports(properties.getMode())) continue;
        try {
          return gateway.store(request);
        } catch (RuntimeException error) {
          lastError = error;
        }
      }
    }
    throw lastError != null ? lastError : new IllegalStateException("没有可用的 IPFS 存储网关");
  }
}
