package pl.bartek.librarymanagementapi.borrows.exceptions;

public class BorrowNotFoundException extends RuntimeException {
  public BorrowNotFoundException(Integer id) {
    super("Borrow with id " + id + " not found.");
  }
}
