package pl.bartek.librarymanagementapi.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.bartek.librarymanagementapi.staff.StaffAccount;
import pl.bartek.librarymanagementapi.staff.StaffAccountRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final StaffAccountRepository staffAccountRepository;

  public CustomUserDetailsService(StaffAccountRepository staffAccountRepository) {
    this.staffAccountRepository = staffAccountRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    StaffAccount account = staffAccountRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials."));
    return User.withUsername(account.getUsername())
            .password(account.getPasswordHash())
            .roles(account.getRole().name())
            .disabled(!account.isEnabled())
            .build();
  }
}
