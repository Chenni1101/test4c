package edu.suibe.evidence.service;

public record ChainAuthorizationRequest(
    String authorizationNo, String assetCode, String assetVersionId, String licenseeName,
    String usageType, boolean commercial, String startsAt, String endsAt, String operator) {}
