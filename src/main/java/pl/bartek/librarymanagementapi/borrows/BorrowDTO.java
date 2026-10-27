package pl.bartek.librarymanagementapi.borrows;

import java.time.LocalDate;

public record BorrowDTO(Integer id,
                        Integer bookId,
                        Integer readerId,
                        LocalDate borrowDate,
                        LocalDate returnDate) {
  public static BorrowDTO fromBorrow(Borrow borrow) {
    return new BorrowDTO(
            borrow.getId(),
            borrow.getBook().getId(),
            borrow.getReader().getId(),
            borrow.getBorrowDate(),
            borrow.getReturnDate()
    );
  }
}