package pl.bartek.librarymanagementapi.logs;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import pl.bartek.librarymanagementapi.books.Books;
import pl.bartek.librarymanagementapi.readers.Reader;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
public class Logs {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ColumnDefault("(curdate())")
  @Column(name = "date", nullable = false)
  private LocalDateTime date;

  @Column(name = "description", nullable = false)
  private String description;

  @ManyToOne
  @JoinColumn(name = "reader_id")
  private Reader reader;

  @ManyToOne
  @JoinColumn(name = "book_id")
  private Books book;

  public Logs() {
  }

  public Logs(LocalDateTime date, String description, Reader reader, Books book) {
    this.date = date;
    this.description = description;
    this.reader = reader;
    this.book = book;
  }

  public Integer getId() {
    return id;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Reader getReader() {
    return reader;
  }

  public void setReader(Reader reader) {
    this.reader = reader;
  }

  public Books getBook() {
    return book;
  }

  public void setBook(Books book) {
    this.book = book;
  }

}
