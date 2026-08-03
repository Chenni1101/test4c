package edu.suibe.evidence.dto;

import java.time.Instant;

public record ChainTransactionResponse(
    Long id,
    String assetVersionId,
    String txId,
    String status,
    String provider,
    String mode,
    String network,
    Integer attemptNo,
    Long blockHeight,
    Instant confirmedAt,
    String errorCode,
    String errorMessage,
    String receipt) {}
