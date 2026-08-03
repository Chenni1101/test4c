package edu.suibe.evidence.controller;

import edu.suibe.evidence.dto.AuthTokenResponse;
import edu.suibe.evidence.dto.LoginRequest;
import edu.suibe.evidence.dto.RegisterRequest;
import edu.suibe.evidence.dto.UserResponse;
import edu.suibe.evidence.dto.UpdateUserRoleRequest;
import edu.suibe.evidence.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) { this.authService = authService; }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest request) { return authService.register(request); }

  @PostMapping("/login")
  public AuthTokenResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request); }

  @GetMapping("/me")
  public UserResponse me() { return authService.currentUser(); }

  @GetMapping("/users")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public List<UserResponse> users() { return authService.listUsers(); }

  @PutMapping("/users/{userId}/role")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public UserResponse updateRole(
      @PathVariable("userId") @Positive Long userId, @Valid @RequestBody UpdateUserRoleRequest request) {
    return authService.updateRole(userId, request);
  }
}
