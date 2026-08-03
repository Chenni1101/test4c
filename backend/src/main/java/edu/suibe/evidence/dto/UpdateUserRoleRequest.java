package edu.suibe.evidence.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRoleRequest(
    @NotBlank
        @Pattern(regexp = "^(GUEST|CREATOR|MUSEUM_ADMIN|SUPER_ADMIN)$")
        String role) {}
