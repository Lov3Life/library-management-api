package pl.bartek.librarymanagementapi.staff.exceptions;

public class StaffConflictException extends RuntimeException {
  public StaffConflictException(String string) {
    super(string);
  }
}
