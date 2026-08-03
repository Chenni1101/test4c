package edu.suibe.evidence.dto;

public record AssetCredentialResponse(
    String assetCode,
    Long assetId,
    String versionId,
    String contentSha256,
    String cid,
    String storageProvider,
    String storageMode,
    String chainTransactionId,
    String chainTxId,
    String chainStatus,
    String assetStatus,
    String mode,
    String notice) {}
