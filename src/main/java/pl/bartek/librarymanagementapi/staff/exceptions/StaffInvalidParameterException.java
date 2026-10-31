package pl.bartek.librarymanagementapi.staff.exceptions;

public class StaffInvalidParameterException extends RuntimeException {
  public StaffInvalidParameterException(String message) {
    super(message);
  }
}
