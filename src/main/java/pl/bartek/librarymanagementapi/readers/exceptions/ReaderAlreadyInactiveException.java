package pl.bartek.librarymanagementapi.readers.exceptions;

public class ReaderAlreadyInactiveException extends RuntimeException {
  public ReaderAlreadyInactiveException(Integer id) {
    super("Reader with id " + id + " is already Inactive.");
  }
}
