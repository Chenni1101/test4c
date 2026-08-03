package edu.suibe.evidence.service;

public record ChainAssetIssueRequest(
    String assetCode,
    String contentSha256,
    String cid,
    String ownerUsername,
    String timestampIso) {}
