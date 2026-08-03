package edu.suibe.evidence.dto;

import java.time.Instant;

public record TimelineEventResponse(Instant occurredAt, String type, String status, String reference, String detail, String mode) {}
