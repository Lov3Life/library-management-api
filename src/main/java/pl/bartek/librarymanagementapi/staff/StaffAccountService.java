package pl.bartek.librarymanagementapi.staff;

public interface StaffAccountService {

  StaffAccountDTO createAccount(StaffAccountCreateRequest request);

  StaffAccountDTO enableAccount(Integer id);

  StaffAccountDTO disableAccount(Integer id);
}
