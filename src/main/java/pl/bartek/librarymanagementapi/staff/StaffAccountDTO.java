package pl.bartek.librarymanagementapi.staff;

public record StaffAccountDTO(
        Integer id,
        String username,
        StaffAccountRole role,
        boolean enabled
) {
  public static StaffAccountDTO fromStaffAccount(StaffAccount account) {
    return new StaffAccountDTO(
            account.getId(),
            account.getUsername(),
            account.getRole(),
            account.isEnabled()
    );
  }
}
