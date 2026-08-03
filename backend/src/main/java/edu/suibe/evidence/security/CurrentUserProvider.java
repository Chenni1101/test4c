package edu.suibe.evidence.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {
  public UserAccountPrincipal requireUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !(authentication.getPrincipal() instanceof UserAccountPrincipal principal)) {
      throw new AccessDeniedException("需要已认证用户");
    }
    return principal;
  }
}
