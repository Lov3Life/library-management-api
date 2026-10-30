package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookRepository;
import pl.bartek.librarymanagementapi.borrows.Borrow;
import pl.bartek.librarymanagementapi.borrows.BorrowRepository;
import pl.bartek.librarymanagementapi.logs.Log;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.logs.LogRepository;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class BorrowIntegrationTest {

  @Container
  @ServiceConnection
  static MySQLContainer mysql = new MySQLContainer("mysql:8.4.13")
          .withDatabaseName("library_management")
          .withInitScript("schema_test.sql");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LogRepository logRepository;

  @Autowired
  private ReaderRepository readerRepository;

  @Autowired
  private BorrowRepository borrowRepository;

  @Autowired
  private BookRepository bookRepository;

  @BeforeEach
  void cleanDatabase() {
    borrowRepository.deleteAll();
    logRepository.deleteAll();
    readerRepository.deleteAll();
    bookRepository.deleteAll();
  }

  private static final Clock FIXED_CLOCK = Clock.fixed(
          Instant.parse("2022-01-01T00:00:00Z"),
          ZoneId.of("Europe/Warsaw"));

  private static final LocalDate TODAY = LocalDate.now(FIXED_CLOCK);

  @Test
  void shouldBorrowBookAndPersistAuditLog() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "Lord of the Rings", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    String request = """
            {
              "bookId": %d,
              "readerId": %d
            }
            """.formatted(testedBook.getId(), testedReader.getId());
    mockMvc.perform(post("/api/borrows")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isCreated());
    Borrow testedBorrow = borrowRepository.findAll().getFirst();
    assertEquals(testedBorrow.getBook().getId(), testedBook.getId());
    assertEquals(testedBorrow.getReader().getId(), testedReader.getId());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log log = logs.getFirst();
    assertTrue(log.getDescription().contains("borrow"));
    assertEquals(testedBorrow.getReader().getId(), log.getReader().getId());
    assertEquals(testedBorrow.getBook().getId(), log.getBook().getId());
    assertEquals(LogEventType.BOOK_BORROWED, log.getEventType());
  }

  @Test
  void shouldReturnBookAndPersistAuditLog() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "Lord of the Rings", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    Borrow testedBorrow = borrowRepository.save(new Borrow(testedBook, testedReader, TODAY));
    mockMvc.perform(patch("/api/borrows/{id}/return", testedBorrow.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    testedBorrow = borrowRepository.findByFilter(testedReader.getId(), testedBook.getId(), true).getFirst();
    assertNotNull(testedBorrow.getReturnDate());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log log = logs.getFirst();
    assertTrue(log.getDescription().contains("return"));
    assertEquals(testedBorrow.getReader().getId(), log.getReader().getId());
    assertEquals(testedBorrow.getBook().getId(), log.getBook().getId());
    assertEquals(LogEventType.BOOK_RETURNED, log.getEventType());
  }

  @Test
  void shouldNotReturnAlreadyReturnedBook() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "Lord of the Rings", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    Borrow testedBorrow = borrowRepository.save(new Borrow(testedBook, testedReader, TODAY));
    mockMvc.perform(patch("/api/borrows/{id}/return", testedBorrow.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    mockMvc.perform(patch("/api/borrows/{id}/return", testedBorrow.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
  }

  @Test
  void shouldNotBorrowAlreadyBorrowedBook() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "Lord of the Rings", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    String request = """
            {
              "bookId": %d,
              "readerId": %d
            }
            """.formatted(testedBook.getId(), testedReader.getId());
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isCreated());
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isConflict());
    List<Borrow> borrows = borrowRepository.findAll();
    assertEquals(1, borrows.size());
  }
}
