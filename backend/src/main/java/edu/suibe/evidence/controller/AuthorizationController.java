package edu.suibe.evidence.controller;

import edu.suibe.evidence.dto.AuthorizationCreateRequest;
import edu.suibe.evidence.dto.AuthorizationResponse;
import edu.suibe.evidence.dto.AuthorizationRevokeRequest;
import edu.suibe.evidence.dto.BatchAuthorizationCreateRequest;
import edu.suibe.evidence.service.AssetLifecycleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/authorizations")
public class AuthorizationController {
  private final AssetLifecycleService lifecycle;
  public AuthorizationController(AssetLifecycleService lifecycle) { this.lifecycle = lifecycle; }

  @PostMapping
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public AuthorizationResponse create(@Valid @RequestBody AuthorizationCreateRequest request) {
    return lifecycle.createAuthorization(request);
  }

  @PostMapping("/batch")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public List<AuthorizationResponse> createBatch(@Valid @RequestBody BatchAuthorizationCreateRequest request) {
    return lifecycle.createAuthorizations(request.authorizations());
  }

  @PostMapping("/{authorizationId}/revoke")
  @PreAuthorize("hasAnyRole('CREATOR', 'MUSEUM_ADMIN', 'SUPER_ADMIN')")
  public AuthorizationResponse revoke(
      @PathVariable("authorizationId") @NotBlank @Pattern(regexp = "^[0-9a-fA-F-]{36}$") String authorizationId,
      @Valid @RequestBody AuthorizationRevokeRequest request) {
    return lifecycle.revoke(authorizationId, request.reason());
  }
}
