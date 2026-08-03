package edu.suibe.evidence.dto;

public record AssetVersionResponse(
    String id, String assetCode, int versionNo, String contentSha256, String cid,
    String storageMode, String storageStatus, String status, String changeNote, String notice) {}
