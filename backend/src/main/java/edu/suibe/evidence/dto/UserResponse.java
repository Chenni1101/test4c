package edu.suibe.evidence.dto;

import java.time.Instant;
import java.util.List;

public record UserResponse(
    Long id,
    String username,
    String displayName,
    String email,
    String organization,
    String status,
    List<String> roles,
    Instant createdAt) {}
