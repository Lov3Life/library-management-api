package pl.bartek.librarymanagementapi.borrows;

import java.util.List;

public interface BorrowService {

  List<BorrowDTO> getBorrows(Integer readerId, Integer bookId, Boolean returned);

  BorrowDTO getBorrow(Integer id);

  BorrowDTO createBorrow(BorrowCreateRequest request);

  BorrowDTO returnBook(Integer id);
}
