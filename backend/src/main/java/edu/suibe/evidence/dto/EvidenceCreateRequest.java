package edu.suibe.evidence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EvidenceCreateRequest(
    @NotBlank @Size(max = 128) String assetName,
    @NotBlank @Size(max = 32) String assetType,
    @NotBlank @Size(max = 128) String creator,
    @Size(max = 128) String organization,
    @Size(max = 4000) String description,
    List<String> keywords,
    @NotBlank
        @Pattern(
            regexp = "^sha256:[a-fA-F0-9]{64}$",
            message = "fileHash 必须是 sha256: 前缀的 64 位十六进制摘要")
        String fileHash,
    @Size(max = 128) String cid,
    @Size(max = 128) String ownerName,
    @NotBlank
        @Pattern(
            regexp = "^\\d{4}-\\d{2}-\\d{2}T.*$",
            message = "timestampIso 必须是 ISO-8601 时间字符串")
        String timestampIso) {}
