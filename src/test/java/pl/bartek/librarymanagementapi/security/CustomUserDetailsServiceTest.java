package pl.bartek.librarymanagementapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.bartek.librarymanagementapi.staff.StaffAccount;
import pl.bartek.librarymanagementapi.staff.StaffAccountRepository;
import pl.bartek.librarymanagementapi.staff.StaffAccountRole;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

  private CustomUserDetailsService customUserDetailsService;

  @Mock
  private StaffAccountRepository staffAccountRepository;

  @BeforeEach
  void setUp() {
    customUserDetailsService = new CustomUserDetailsService(staffAccountRepository);
  }

  @Test
  void shouldReturnUserDetailsForLibrarian() {
    StaffAccount account = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.LIBRARIAN);
    when(staffAccountRepository.findByUsername("test"))
            .thenReturn(Optional.of(account));
    UserDetails userDetails = customUserDetailsService.loadUserByUsername("test");
    assertEquals("test", userDetails.getUsername());
    assertEquals("LibrarianPassword123!", userDetails.getPassword());
    assertEquals(1, userDetails.getAuthorities().size());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LIBRARIAN")));
  }

  @Test
  void shouldReturnUserDetailsForAdmin() {
    StaffAccount account = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.ADMIN);
    when(staffAccountRepository.findByUsername("test"))
            .thenReturn(Optional.of(account));
    UserDetails userDetails = customUserDetailsService.loadUserByUsername("test");
    assertEquals("test", userDetails.getUsername());
    assertEquals("LibrarianPassword123!", userDetails.getPassword());
    assertEquals(1, userDetails.getAuthorities().size());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
  }

  @Test
  void shouldReturnDisabledUserDetails() {
    StaffAccount account = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.LIBRARIAN);
    account.setEnabled(false);
    when(staffAccountRepository.findByUsername("test"))
            .thenReturn(Optional.of(account));
    UserDetails userDetails = customUserDetailsService.loadUserByUsername("test");
    assertEquals("test", userDetails.getUsername());
    assertEquals("LibrarianPassword123!", userDetails.getPassword());
    assertEquals(1, userDetails.getAuthorities().size());
    assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LIBRARIAN")));
  }

  @Test
  void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
    when(staffAccountRepository.findByUsername("test"))
            .thenReturn(Optional.empty());
    assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("test"));
  }
}
