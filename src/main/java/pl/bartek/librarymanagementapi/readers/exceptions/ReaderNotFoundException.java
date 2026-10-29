package pl.bartek.librarymanagementapi.readers.exceptions;

public class ReaderNotFoundException extends RuntimeException {
  public ReaderNotFoundException(Integer id) {
    super("Reader with id: " + id + " not found.");
  }
}
