package pl.bartek.librarymanagementapi.staff;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffConflictException;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffNotFoundException;

@Service
public class StaffAccountServiceImpl implements StaffAccountService{

  private final StaffAccountRepository staffAccountRepository;
  private final PasswordEncoder passwordEncoder;

  public StaffAccountServiceImpl(StaffAccountRepository staffAccountRepository, PasswordEncoder passwordEncoder) {
    this.staffAccountRepository = staffAccountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public StaffAccountDTO createAccount(StaffAccountCreateRequest request) {
    String username = request.username().trim();
    if(staffAccountRepository.findByUsername(username).isPresent())
      throw new StaffConflictException("Username is already taken.");
    String passwordHash = passwordEncoder.encode(request.password());
    StaffAccount account = new StaffAccount(username, passwordHash, StaffAccountRole.LIBRARIAN);
    return StaffAccountDTO.fromStaffAccount(staffAccountRepository.save(account));
  }

  @Override
  @Transactional
  public StaffAccountDTO enableAccount(Integer id) {
    StaffAccount account = staffAccountRepository.findById(id).orElseThrow(
            () -> new StaffNotFoundException("Staff account with id: " + id + " not found.")
    );
    if(account.isEnabled())
      throw new StaffConflictException("Staff account with id: " + id + " is already enabled.");
    account.setEnabled(true);
    return StaffAccountDTO.fromStaffAccount(staffAccountRepository.save(account));
  }

  @Override
  @Transactional
  public StaffAccountDTO disableAccount(Integer id) {
    StaffAccount account = staffAccountRepository.findById(id).orElseThrow(
            () -> new StaffNotFoundException("Staff account with id: " + id + " not found.")
    );
    if (account.getRole() == StaffAccountRole.ADMIN)
      throw new StaffConflictException("Administrator account cannot be disabled.");
    if(!account.isEnabled())
      throw new StaffConflictException("Staff account with id: " + id + " is already disabled.");
    account.setEnabled(false);
    return StaffAccountDTO.fromStaffAccount(staffAccountRepository.save(account));
  }
}
