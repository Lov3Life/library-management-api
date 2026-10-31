package pl.bartek.librarymanagementapi.staff;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
public class StaffAccountController {

  private final StaffAccountService staffAccountService;

  public StaffAccountController(StaffAccountService staffAccountService) {
    this.staffAccountService = staffAccountService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public StaffAccountDTO createStaffAccount(@Valid @RequestBody StaffAccountCreateRequest request) {
    return staffAccountService.createAccount(request);
  }

  @PatchMapping("/{id}/enable")
  public StaffAccountDTO enableAccount(@PathVariable Integer id) {
    return staffAccountService.enableAccount(id);
  }

  @PatchMapping("/{id}/disable")
  public StaffAccountDTO disableAccount(@PathVariable Integer id) {
    return staffAccountService.disableAccount(id);
  }
}
