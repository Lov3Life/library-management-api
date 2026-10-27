package pl.bartek.librarymanagementapi.borrows.exceptions;

public class BorrowConflictException extends RuntimeException {
  public BorrowConflictException(String message) {
    super(message);
  }
}
