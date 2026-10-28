package pl.bartek.librarymanagementapi.books.exceptions;

public class BookHasActiveBorrowsException extends RuntimeException {
  public BookHasActiveBorrowsException(Integer id) {
    super("Book with id " + id + " is borrowed. Cannot be deactivated.");
  }
}
