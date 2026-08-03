package edu.suibe.evidence.security;

import edu.suibe.evidence.entity.RoleEntity;
import edu.suibe.evidence.entity.UserEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserAccountPrincipal implements UserDetails {
  private final Long id;
  private final String username;
  private final String passwordHash;
  private final String organization;
  private final String status;
  private final List<GrantedAuthority> authorities;

  private UserAccountPrincipal(
      Long id,
      String username,
      String passwordHash,
      String organization,
      String status,
      List<GrantedAuthority> authorities) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.organization = organization;
    this.status = status;
    this.authorities = authorities;
  }

  public static UserAccountPrincipal from(UserEntity user) {
    List<GrantedAuthority> authorities =
        user.getRoles().stream()
            .map(RoleEntity::getCode)
            .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
            .map(GrantedAuthority.class::cast)
            .toList();
    return new UserAccountPrincipal(
        user.getId(),
        user.getUsername(),
        user.getPasswordHash(),
        user.getOrganization(),
        user.getStatus(),
        authorities);
  }

  public Long getId() { return id; }
  public String getOrganization() { return organization; }
  public boolean hasRole(String role) {
    return authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
  }
  public List<String> roleCodes() {
    return authorities.stream().map(GrantedAuthority::getAuthority).map(role -> role.substring(5)).toList();
  }
  @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
  @Override public String getPassword() { return passwordHash; }
  @Override public String getUsername() { return username; }
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return "ACTIVE".equals(status); }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return "ACTIVE".equals(status); }
}
