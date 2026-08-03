package edu.suibe.evidence.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Returns the authenticated subject for audit records instead of trusting request payload fields. */
@Component
public class CurrentActorProvider {
  public String actorName() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication instanceof AnonymousAuthenticationToken) {
      return "anonymous";
    }
    return authentication.getName();
  }
}
