package pl.bartek.librarymanagementapi.books.exceptions;

public class BookAlreadyActiveException extends RuntimeException {
  public BookAlreadyActiveException(Integer id) {
    super("Book with id " + id + " is already active.");
  }
}
