package edu.suibe.evidence.dto;

public record FileHashVerificationResponse(
    String computedSha256, String expectedSha256, boolean matches, String assetCode, String versionId, String notice) {}
