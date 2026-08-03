package edu.suibe.evidence.dto;

import java.util.List;

public record AuthTokenResponse(String accessToken, String tokenType, long expiresIn, List<String> roles) {}
