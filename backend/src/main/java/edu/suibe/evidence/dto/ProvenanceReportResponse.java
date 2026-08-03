package edu.suibe.evidence.dto;

import java.util.List;

public record ProvenanceReportResponse(String assetCode, String assetStatus, String mode, String disclaimer, List<TimelineEventResponse> timeline) {}
