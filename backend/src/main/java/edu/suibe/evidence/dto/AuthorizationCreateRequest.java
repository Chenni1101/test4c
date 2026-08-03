package edu.suibe.evidence.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public record AuthorizationCreateRequest(
    @NotBlank @Pattern(regexp = "^AST-[0-9]{8}-[A-Z0-9]{6}$") String assetCode,
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$", message = "assetVersionId 必须为 UUID") String assetVersionId,
    @NotBlank @Size(max = 255) String licenseeName,
    @Size(max = 255) String licenseeOrganization,
    @NotBlank @Pattern(regexp = "^(COMMERCIAL|NON_COMMERCIAL)$") String usageType,
    boolean commercial,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal revenueShareRatio,
    @NotNull Instant startsAt,
    @NotNull @Future Instant endsAt) {}
