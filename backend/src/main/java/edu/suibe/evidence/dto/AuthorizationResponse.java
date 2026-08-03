package edu.suibe.evidence.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AuthorizationResponse(
    String id, String authorizationNo, String assetCode, String assetVersionId,
    String licenseeName, String licenseeOrganization, String usageType, boolean commercial,
    BigDecimal revenueShareRatio, Instant startsAt, Instant endsAt, String status,
    Long chainTransactionId, String chainTxId, String mode, Instant revokedAt, String revokeReason,
    String notice) {}
