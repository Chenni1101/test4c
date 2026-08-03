package edu.suibe.evidence.dto;

public record IpfsFileResponse(
    String id,
    String assetVersionId,
    String cid,
    String provider,
    String mode,
    String pinStatus,
    String contentSha256,
    String notice) {}
