package pl.bartek.librarymanagementapi.logs;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.readers.Reader;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
public class Log {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "date", nullable = false)
  private LocalDateTime date;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(name = "event_type", nullable = false, length = 50)
  private LogEventType eventType;

  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

  @ManyToOne
  @JoinColumn(name = "reader_id")
  private Reader reader;

  @ManyToOne
  @JoinColumn(name = "book_id")
  private Book book;

  public Log() {
  }

  public Log(LocalDateTime date, LogEventType eventType, String description, Reader reader, Book book) {
    this.date = date;
    this.eventType = eventType;
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

  public LogEventType getEventType() {
    return eventType;
  }

  public void setEventType(LogEventType eventType) {
    this.eventType = eventType;
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

  public Book getBook() {
    return book;
  }

  public void setBook(Book book) {
    this.book = book;
  }

  @Override
  public String toString() {
    return "Log{" +
            "id=" + id +
            ", date=" + date +
            ", eventType=" + eventType +
            ", description='" + description + '\'' +
            '}';
  }
}
