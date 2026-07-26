package edu.suibe.evidence.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record EvidenceCreateRequest(
    @NotBlank String assetName,
    @NotBlank String assetType,
    @NotBlank String creator,
    String organization,
    String description,
    List<String> keywords,
    @NotBlank String fileHash,
    String cid,
    @NotBlank String ownerName,
    @NotBlank String timestampIso) {}
