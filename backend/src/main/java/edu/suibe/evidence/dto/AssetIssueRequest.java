package edu.suibe.evidence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AssetIssueRequest(
    @NotBlank @Size(max = 128) String assetName,
    @NotBlank @Size(max = 32) String assetType,
    @NotBlank @Size(max = 128) String creator,
    @Size(max = 128) String organization,
    @Size(max = 4000) String description,
    List<@Size(max = 64) String> keywords,
    @NotBlank
        @Pattern(regexp = "^(sha256:)?[a-fA-F0-9]{64}$", message = "contentSha256 必须是 SHA-256 摘要")
        String contentSha256,
    @NotBlank @Size(max = 512) String originalFilename,
    @NotBlank @Size(max = 127) String mimeType,
    @PositiveOrZero long fileSizeBytes) {}
