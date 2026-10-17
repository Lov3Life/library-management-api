package pl.bartek.librarymanagementapi.books.exceptions;

public class BookCreateInvalidParameterException extends RuntimeException {
  public BookCreateInvalidParameterException(String message) {
    super(message);
  }
}
