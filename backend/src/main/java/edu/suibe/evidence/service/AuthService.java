package edu.suibe.evidence.service;

import edu.suibe.evidence.config.BootstrapProperties;
import edu.suibe.evidence.config.JwtProperties;
import edu.suibe.evidence.dto.AuthTokenResponse;
import edu.suibe.evidence.dto.LoginRequest;
import edu.suibe.evidence.dto.RegisterRequest;
import edu.suibe.evidence.dto.UserResponse;
import edu.suibe.evidence.dto.UpdateUserRoleRequest;
import edu.suibe.evidence.entity.RoleEntity;
import edu.suibe.evidence.entity.UserEntity;
import edu.suibe.evidence.repository.RoleRepository;
import edu.suibe.evidence.repository.UserRepository;
import edu.suibe.evidence.security.CurrentUserProvider;
import edu.suibe.evidence.security.JwtService;
import edu.suibe.evidence.security.UserAccountPrincipal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final CurrentUserProvider currentUserProvider;
  private final AuditService auditService;

  public AuthService(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      JwtProperties jwtProperties,
      CurrentUserProvider currentUserProvider,
      AuditService auditService) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.currentUserProvider = currentUserProvider;
    this.auditService = auditService;
  }

  @Transactional
  public UserResponse register(RegisterRequest request) {
    String username = request.username().trim().toLowerCase(Locale.ROOT);
    String email = request.email().trim().toLowerCase(Locale.ROOT);
    if (userRepository.existsByUsername(username) || userRepository.existsByEmail(email)) {
      throw new DataIntegrityViolationException("用户名或邮箱已存在");
    }
    String roleCode = request.role() == null || request.role().isBlank() ? "CREATOR" : request.role();
    RoleEntity role = roleRepository.findByCode(roleCode).orElseThrow(() -> new IllegalStateException("系统角色未初始化"));
    UserEntity user = new UserEntity();
    user.setUsername(username);
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setDisplayName(request.displayName().trim());
    user.setEmail(email);
    user.setOrganization(blankToNull(request.organization()));
    user.setRoleCode(roleCode);
    user.setStatus("ACTIVE");
    user.setRoles(new LinkedHashSet<>(List.of(role)));
    UserEntity saved = userRepository.save(user);
    auditService.record("USER_REGISTER", saved.getUsername(), "user:" + saved.getId(), "role=" + roleCode);
    return toResponse(saved, false);
  }

  @Transactional
  public AuthTokenResponse login(LoginRequest request) {
    String username = request.username().trim().toLowerCase(Locale.ROOT);
    try {
      Authentication authentication =
          authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
      UserAccountPrincipal principal = (UserAccountPrincipal) authentication.getPrincipal();
      UserEntity user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
      user.setLastLoginAt(Instant.now());
      auditService.record("LOGIN_SUCCESS", principal.getUsername(), "user:" + principal.getId(), "jwt-issued");
      return new AuthTokenResponse(
          jwtService.generate(principal), "Bearer", jwtProperties.getExpirationMinutes() * 60, principal.roleCodes());
    } catch (AuthenticationException exception) {
      auditService.record("LOGIN_FAILURE", username, "user:" + username, "invalid-credentials");
      throw new BadCredentialsException("用户名或密码错误");
    }
  }

  @Transactional(readOnly = true)
  public UserResponse currentUser() {
    UserAccountPrincipal principal = currentUserProvider.requireUser();
    UserEntity user = userRepository.findByUsername(principal.getUsername()).orElseThrow();
    return toResponse(user, false);
  }

  @Transactional(readOnly = true)
  public List<UserResponse> listUsers() {
    return userRepository.findAll().stream().map(user -> toResponse(user, true)).toList();
  }

  @Transactional
  public UserResponse updateRole(Long userId, UpdateUserRoleRequest request) {
    UserAccountPrincipal actor = currentUserProvider.requireUser();
    if (!actor.hasRole("SUPER_ADMIN")) {
      throw new org.springframework.security.access.AccessDeniedException("仅超级管理员可调整角色");
    }
    UserEntity user = userRepository.findById(userId).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("用户不存在"));
    RoleEntity role = roleRepository.findByCode(request.role()).orElseThrow(() -> new IllegalStateException("系统角色未初始化"));
    user.setRoleCode(role.getCode());
    if (user.getRoles() == null) {
      user.setRoles(new LinkedHashSet<>());
    } else {
      user.getRoles().clear();
    }
    user.getRoles().add(role);
    auditService.record("USER_ROLE_UPDATE", actor.getUsername(), "user:" + userId, "role=" + role.getCode());
    return toResponse(user, true);
  }

  private UserResponse toResponse(UserEntity user, boolean maskEmail) {
    List<String> roles = user.getRoles().stream().map(RoleEntity::getCode).sorted().toList();
    return new UserResponse(
        user.getId(), user.getUsername(), user.getDisplayName(), maskEmail(user.getEmail(), maskEmail),
        user.getOrganization(), user.getStatus(), roles, user.getCreatedAt());
  }

  private String maskEmail(String email, boolean shouldMask) {
    if (!shouldMask || email == null) return email;
    int at = email.indexOf('@');
    if (at <= 1) return "***";
    return email.substring(0, 1) + "***" + email.substring(at);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  @Configuration
  static class BootstrapAdminConfiguration {
    @Bean
    CommandLineRunner bootstrapSuperAdmin(
        BootstrapProperties properties,
        UserRepository userRepository,
        RoleRepository roleRepository,
        PasswordEncoder passwordEncoder,
        AuditService auditService) {
      return args -> {
        if (!properties.isEnabled() || userRepository.existsByUsername(properties.getUsername())) return;
        RoleEntity role = roleRepository.findByCode("SUPER_ADMIN").orElseThrow();
        UserEntity user = new UserEntity();
        user.setUsername(properties.getUsername().toLowerCase(Locale.ROOT));
        user.setPasswordHash(passwordEncoder.encode(properties.getPassword()));
        user.setDisplayName("本地演示超级管理员");
        user.setOrganization(properties.getOrganization());
        user.setRoleCode("SUPER_ADMIN");
        user.setStatus("ACTIVE");
        user.setRoles(new LinkedHashSet<>(List.of(role)));
        UserEntity saved = userRepository.save(user);
        auditService.record("BOOTSTRAP_SUPER_ADMIN", "system", "user:" + saved.getId(), "local-demo-only");
      };
    }
  }
}
