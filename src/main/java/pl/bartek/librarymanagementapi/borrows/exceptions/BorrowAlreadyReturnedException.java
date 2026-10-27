package pl.bartek.librarymanagementapi.borrows.exceptions;

public class BorrowAlreadyReturnedException extends RuntimeException {
  public BorrowAlreadyReturnedException(String string) {
    super(string);
  }
}
