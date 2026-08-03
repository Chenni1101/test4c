package edu.suibe.evidence.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,64}$") String username,
    @NotBlank
        @Size(min = 12, max = 72)
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码必须同时包含字母和数字")
        String password,
    @NotBlank @Size(max = 128) String displayName,
    @NotBlank @Email @Size(max = 254) String email,
    @Size(max = 128) String organization,
    @Pattern(regexp = "^(GUEST|CREATOR)?$", message = "注册角色仅允许 GUEST 或 CREATOR") String role) {}
