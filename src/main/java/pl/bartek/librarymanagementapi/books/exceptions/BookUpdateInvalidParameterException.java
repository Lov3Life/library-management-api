package pl.bartek.librarymanagementapi.books.exceptions;

public class BookUpdateInvalidParameterException extends RuntimeException {
  public BookUpdateInvalidParameterException(String message) {
    super(message);
  }
}
