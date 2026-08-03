package edu.suibe.evidence.service;

public record IpfsStoreResult(String cid, String provider, String mode, String gatewayUrl, String status) {}
