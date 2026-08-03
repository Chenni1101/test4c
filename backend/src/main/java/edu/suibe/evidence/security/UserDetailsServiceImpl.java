package edu.suibe.evidence.security;

import edu.suibe.evidence.entity.UserEntity;
import edu.suibe.evidence.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  public UserDetailsServiceImpl(UserRepository userRepository) { this.userRepository = userRepository; }

  @Override
  public UserAccountPrincipal loadUserByUsername(String username) {
    UserEntity user =
        userRepository
            .findByUsername(username.toLowerCase())
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在或已禁用"));
    return UserAccountPrincipal.from(user);
  }
}
