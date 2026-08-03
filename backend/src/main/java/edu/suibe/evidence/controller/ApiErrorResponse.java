package edu.suibe.evidence.controller;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String code,
    String message,
    String traceId,
    Map<String, String> fieldErrors) {}
