package edu.suibe.evidence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record AssetVersionCreateRequest(
    @NotBlank @Pattern(regexp = "^(sha256:)?[a-fA-F0-9]{64}$") String contentSha256,
    @NotBlank @Size(max = 512) String originalFilename,
    @NotBlank @Size(max = 127) String mimeType,
    @PositiveOrZero long fileSizeBytes,
    @Size(max = 1000) String changeNote) {}
