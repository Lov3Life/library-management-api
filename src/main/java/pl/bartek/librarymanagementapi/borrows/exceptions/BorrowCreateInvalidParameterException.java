package pl.bartek.librarymanagementapi.borrows.exceptions;

public class BorrowCreateInvalidParameterException extends RuntimeException {
  public BorrowCreateInvalidParameterException(String message) {
    super(message);
  }
}
