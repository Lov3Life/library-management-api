package pl.bartek.librarymanagementapi.staff.exceptions;

public class StaffNotFoundException extends RuntimeException {
  public StaffNotFoundException(String string) {
    super(string);
  }
}
