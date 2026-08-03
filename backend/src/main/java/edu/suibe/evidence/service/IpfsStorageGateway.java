package edu.suibe.evidence.service;

public interface IpfsStorageGateway {
  String provider();
  boolean supports(String mode);
  IpfsStoreResult store(IpfsStoreRequest request);
}
