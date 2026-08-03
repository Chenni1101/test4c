package edu.suibe.evidence.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record BatchAuthorizationCreateRequest(
    @NotEmpty @Size(max = 10) List<@Valid AuthorizationCreateRequest> authorizations) {}
