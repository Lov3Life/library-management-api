package pl.bartek.librarymanagementapi.borrows;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Integer> {

  boolean existsByBookIdAndReturnDateIsNull(Integer bookId);

  boolean existsByReaderIdAndReturnDateIsNull(Integer readerId);

  @Query("""
        SELECT b
        FROM Borrow b
        WHERE (:readerId IS NULL OR b.reader.id = :readerId)
          AND (:bookId IS NULL OR b.book.id = :bookId)
          AND (
              :returned IS NULL
              OR (:returned = true AND b.returnDate IS NOT NULL)
              OR (:returned = false AND b.returnDate IS NULL)
          )
        """)
  List<Borrow> findByFilter(@Param("readerId") Integer readerId,
                            @Param("bookId") Integer bookId,
                            @Param("returned") Boolean returned);
}
