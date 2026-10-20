package pl.bartek.librarymanagementapi.readers.exceptions;

public class ReaderAlreadyActiveException extends RuntimeException {
  public ReaderAlreadyActiveException(Integer id) {
    super("Reader with id " + id + " is already active");
  }
}
