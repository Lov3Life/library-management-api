package pl.bartek.librarymanagementapi.books.exceptions;

public class BookAlreadyInactiveException extends RuntimeException {
  public BookAlreadyInactiveException(Integer id) {
    super("Book with id " + id + " is already Inactive.");
  }
}
