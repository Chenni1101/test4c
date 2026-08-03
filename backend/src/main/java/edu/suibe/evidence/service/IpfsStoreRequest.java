package edu.suibe.evidence.service;

public record IpfsStoreRequest(String contentSha256, String originalFilename, String mimeType, long sizeBytes) {}
