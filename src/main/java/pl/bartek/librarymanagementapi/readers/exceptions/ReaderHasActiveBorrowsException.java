package pl.bartek.librarymanagementapi.readers.exceptions;

public class ReaderHasActiveBorrowsException extends RuntimeException {
  public ReaderHasActiveBorrowsException(Integer id) {
    super("Reader with id " + id + " has active borrows. Cannot be deactivated.");
  }
}
