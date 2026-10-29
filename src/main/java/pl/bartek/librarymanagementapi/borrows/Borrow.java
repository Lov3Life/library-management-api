package pl.bartek.librarymanagementapi.borrows;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.readers.Reader;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "borrows")
public class Borrow {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "reader_id", nullable = false)
  private Reader reader;

  @ColumnDefault("(curdate())")
  @Column(name = "borrow_date", nullable = false)
  private LocalDate borrowDate;

  @Column(name = "return_date")
  private LocalDate returnDate;

  public Borrow() {
  }

  public Borrow(Book book, Reader reader, LocalDate borrowDate) {
    this.book = Objects.requireNonNull(book);
    this.reader = Objects.requireNonNull(reader);
    this.borrowDate = Objects.requireNonNull(borrowDate);
  }

  public Integer getId() {
    return id;
  }

  public Book getBook() {
    return book;
  }

  public Reader getReader() {
    return reader;
  }

  public LocalDate getBorrowDate() {
    return borrowDate;
  }

  public LocalDate getReturnDate() {
    return returnDate;
  }

  public void returnBook(LocalDate returnDate) {
    Objects.requireNonNull(returnDate);
    if(isReturned()){
      throw new IllegalStateException("Book has already been returned.");
    }
    if(returnDate.isBefore(borrowDate)){
      throw new IllegalArgumentException("Return date cannot be before borrow date.");
    }
    this.returnDate = returnDate;
  }

  public boolean isReturned() {
    return returnDate != null;
  }

  @Override
  public String toString() {
    return "Borrow{" +
            "id=" + id +
            ", borrowDate=" + borrowDate +
            ", returnDate=" + returnDate +
            '}';
  }
}
