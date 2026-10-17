package pl.bartek.librarymanagementapi.books.exceptions;

public class BookNotFoundException extends RuntimeException {
  public BookNotFoundException(Integer id) {
    super("Book with id: " + id + " not found");
  }
}
