package edu.suibe.evidence.dto;

public record EvidenceResponse(
    String assetCode,
    String assetName,
    String assetType,
    String creator,
    String organization,
    String fileHash,
    String cid,
    String uniqueId,
    String txId,
    long blockHeight,
    String chainNetwork,
    String contractName,
    String certifyTime,
    String status,
    String mode) {}
