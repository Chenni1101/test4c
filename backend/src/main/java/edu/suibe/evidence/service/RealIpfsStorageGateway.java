package edu.suibe.evidence.service;

import org.springframework.stereotype.Component;

/** Real provider boundary; no CID is fabricated when no pinned IPFS provider is configured. */
@Component
public class RealIpfsStorageGateway implements IpfsStorageGateway {
  @Override
  public String provider() { return "REAL_IPFS"; }

  @Override
  public boolean supports(String mode) { return "REAL".equalsIgnoreCase(mode); }

  @Override
  public IpfsStoreResult store(IpfsStoreRequest request) {
    throw new IllegalStateException("REAL IPFS Pinning 服务尚未配置");
  }
}
