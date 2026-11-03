package pl.bartek.librarymanagementapi.staff;

import java.util.List;

public interface StaffAccountService {

  List<StaffAccountDTO> getLibrarians();

  StaffAccountDTO createAccount(StaffAccountCreateRequest request);

  StaffAccountDTO enableAccount(Integer id);

  StaffAccountDTO disableAccount(Integer id);
}
