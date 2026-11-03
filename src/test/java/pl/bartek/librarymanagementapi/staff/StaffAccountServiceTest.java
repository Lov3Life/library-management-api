package pl.bartek.librarymanagementapi.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffConflictException;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffInvalidParameterException;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StaffAccountServiceTest {

  @Mock
  private StaffAccountRepository staffAccountRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private StaffAccountServiceImpl staffAccountService;

  @BeforeEach
  void setUp() {
    staffAccountService = new StaffAccountServiceImpl(staffAccountRepository, passwordEncoder);
  }

  @Test
  void shouldReturnLibrariansEmptyList(){
    when(staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN)).thenReturn(List.of());
    List<StaffAccountDTO> result = staffAccountService.getLibrarians();
    assertTrue(result.isEmpty());
  }

  @Test
  void shouldReturnLibrarians(){
    StaffAccount staffAccount = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.LIBRARIAN);
    when(staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN)).thenReturn(List.of(staffAccount));
    List<StaffAccountDTO> result = staffAccountService.getLibrarians();
    assertEquals(1, result.size());
    assertEquals("test", result.getFirst().username());
    assertEquals(StaffAccountRole.LIBRARIAN, result.getFirst().role());
    assertTrue(result.getFirst().enabled());
  }

  @Test
  void shouldCreateStaffAccount() {
    StaffAccountCreateRequest request = new StaffAccountCreateRequest("test", "LibrarianPassword123!");
    when(staffAccountRepository.findByUsername("test"))
            .thenReturn(Optional.empty());
    when(passwordEncoder.encode("LibrarianPassword123!"))
            .thenReturn("encodedPassword");
    when(staffAccountRepository.save(any(StaffAccount.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    StaffAccountDTO result = staffAccountService.createAccount(request);
    assertEquals("test", result.username());
    ArgumentCaptor<StaffAccount> captor = ArgumentCaptor.forClass(StaffAccount.class);
    verify(staffAccountRepository).save(captor.capture());
    StaffAccount savedAccount = captor.getValue();
    assertEquals("encodedPassword", savedAccount.getPasswordHash());
    assertEquals(StaffAccountRole.LIBRARIAN, savedAccount.getRole());
    assertTrue(savedAccount.isEnabled());
  }

  @Test
  void shouldNotCreateStaffAccountWhenUsernameAlreadyExists() {
    StaffAccountCreateRequest request = new StaffAccountCreateRequest("test", "LibrarianPassword123!");
    when(staffAccountRepository.findByUsername("test"))
            .thenReturn(Optional.of(new StaffAccount()));
    assertThrows(StaffConflictException.class, () -> staffAccountService.createAccount(request));
    verify(passwordEncoder, never()).encode("LibrarianPassword123!");
    verify(staffAccountRepository, never()).save(any(StaffAccount.class));
  }

  @Test
  void shouldNotCreateStaffAccountWhenPasswordIsToLong(){
    String password = "a".repeat(200);
    StaffAccountCreateRequest request = new StaffAccountCreateRequest("test", password);
    assertThrows(StaffInvalidParameterException.class, () -> staffAccountService.createAccount(request));
    verify(passwordEncoder, never()).encode(password);
    verify(staffAccountRepository, never()).save(any(StaffAccount.class));
  }

  @Test
  void shouldEnabledTheAccount(){
    StaffAccount staffAccount = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.LIBRARIAN);
    ReflectionTestUtils.setField(staffAccount, "id", 1);
    ReflectionTestUtils.setField(staffAccount, "enabled", false);
    when(staffAccountRepository.findById(1))
            .thenReturn(Optional.of(staffAccount));
    when(staffAccountRepository.save(staffAccount))
            .thenAnswer(invocation -> invocation.getArgument(0));
    staffAccountService.enableAccount(1);
    assertTrue(staffAccount.isEnabled());
    verify(staffAccountRepository).save(staffAccount);
  }

  @Test
  void shouldNotEnabledTheAccountWhenIsAlreadyEnabled(){
    StaffAccount staffAccount = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.LIBRARIAN);
    ReflectionTestUtils.setField(staffAccount, "id", 1);
    when(staffAccountRepository.findById(1))
            .thenReturn(Optional.of(staffAccount));
    assertThrows(StaffConflictException.class, () -> staffAccountService.enableAccount(1));
    assertTrue(staffAccount.isEnabled());
  }

  @Test
  void shouldThrowStaffNotFoundExceptionWhenUserDoesNotExistWhileActivation(){
    when(staffAccountRepository.findById(1))
            .thenReturn(Optional.empty());
    assertThrows(StaffNotFoundException.class, () -> staffAccountService.enableAccount(1));
  }

  @Test
  void shouldDisabledTheAccount(){
    StaffAccount staffAccount = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.LIBRARIAN);
    ReflectionTestUtils.setField(staffAccount, "id", 1);
    when(staffAccountRepository.findById(1))
            .thenReturn(Optional.of(staffAccount));
    when(staffAccountRepository.save(staffAccount))
            .thenAnswer(invocation -> invocation.getArgument(0));
    staffAccountService.disableAccount(1);
    assertFalse(staffAccount.isEnabled());
    verify(staffAccountRepository).save(staffAccount);
  }

  @Test
  void shouldNotDisabledTheAccountWhenIsAlreadyDisabled(){
    StaffAccount staffAccount = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.LIBRARIAN);
    ReflectionTestUtils.setField(staffAccount, "id", 1);
    ReflectionTestUtils.setField(staffAccount, "enabled", false);
    when(staffAccountRepository.findById(1))
            .thenReturn(Optional.of(staffAccount));
    assertThrows(StaffConflictException.class, () -> staffAccountService.disableAccount(1));
    assertFalse(staffAccount.isEnabled());
  }

  @Test
  void shouldNotDisabledTheAccountWhenRoleIsAdmin(){
    StaffAccount staffAccount = new StaffAccount("test", "LibrarianPassword123!", StaffAccountRole.ADMIN);
    ReflectionTestUtils.setField(staffAccount, "id", 1);
    when(staffAccountRepository.findById(1))
            .thenReturn(Optional.of(staffAccount));
    assertThrows(StaffConflictException.class, () -> staffAccountService.disableAccount(1));
    assertTrue(staffAccount.isEnabled());
  }

  @Test
  void shouldThrowStaffNotFoundExceptionWhenUserDoesNotExistWhileDeactivation(){
    when(staffAccountRepository.findById(1))
            .thenReturn(Optional.empty());
    assertThrows(StaffNotFoundException.class, () -> staffAccountService.disableAccount(1));
  }
}

