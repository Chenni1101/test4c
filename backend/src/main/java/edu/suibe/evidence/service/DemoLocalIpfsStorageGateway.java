package edu.suibe.evidence.service;

import java.util.Locale;
import org.springframework.stereotype.Component;

/** Demo-only metadata store. Its demo-cid-* values are explicitly not IPFS CIDs. */
@Component
public class DemoLocalIpfsStorageGateway implements IpfsStorageGateway {
  @Override
  public String provider() { return "DEMO_LOCAL"; }

  @Override
  public boolean supports(String mode) { return "DEMO".equalsIgnoreCase(mode); }

  @Override
  public IpfsStoreResult store(IpfsStoreRequest request) {
    // Keep the complete declared content hash in the demo identifier. Truncating its
    // first 32 characters made different files with a common hash prefix collide.
    String suffix = request.contentSha256().toLowerCase(Locale.ROOT);
    return new IpfsStoreResult("demo-cid-" + suffix, provider(), "DEMO", null, "PINNED");
  }
}
