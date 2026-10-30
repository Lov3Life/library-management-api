package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookDTO;
import pl.bartek.librarymanagementapi.borrows.Borrow;
import pl.bartek.librarymanagementapi.integration.config.MySqlTestConfiguration;
import pl.bartek.librarymanagementapi.logs.LogDescriptionBuilder;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderDTO;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MySqlTestConfiguration.class)
public class TransactionRollbackIntegrationTest extends BaseIntegrationTest {

  @MockitoBean
  private LogDescriptionBuilder logDescriptionBuilder;

  @Test
  void shouldRollbackBookCreationWhenAuditFails() throws Exception{
    when(logDescriptionBuilder.buildMessage(
            eq(LogEventType.BOOK_CREATED),isNull(),any(Book.class), isNull(), isNull()
    )).thenThrow(new IllegalStateException("Simulated Exception"));
    String request = """
            {
              "title": "The Hobbit"
            }
            """;
    mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertInstanceOf(
                    IllegalStateException.class, result.getResolvedException()));
    assertEquals(0, bookRepository.count());
    assertEquals(0, logRepository.count());
  }

  @Test
  void shouldRollbackReaderCreationWhenAuditFails() throws Exception{
    when(logDescriptionBuilder.buildMessage(
            eq(LogEventType.READER_CREATED),any(Reader.class),isNull(), isNull(), isNull())
    ).thenThrow(new IllegalStateException("Simulated Exception"));
    String request = """
            {
              "firstName": "Edward",
              "lastName": "Kenway",
              "birthDate": "1693-03-10"
            }
            """;
    mockMvc.perform(post("/api/readers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertInstanceOf(
                    IllegalStateException.class, result.getResolvedException()));
    assertEquals(0, readerRepository.count());
    assertEquals(0, logRepository.count());
  }

  @Test
  void shouldRollbackBookUpdateWhenAuditFails() throws Exception{
    Book testedBook = bookRepository.save(new Book(
            "The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    when(logDescriptionBuilder.buildMessage(
            eq(LogEventType.BOOK_UPDATED),isNull(),any(Book.class), isNull(), any(BookDTO.class)
    )).thenThrow(new IllegalStateException("Simulated Exception"));
    String request = """
            {
              "title": "Lord of The Rings"
            }
            """;
    mockMvc.perform(patch("/api/books/{id}", testedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertInstanceOf(
                    IllegalStateException.class, result.getResolvedException()));
    Book persistedBook = bookRepository.findById(testedBook.getId()).orElseThrow();
    assertEquals("The Hobbit", persistedBook.getTitle());
    assertEquals(1, bookRepository.count());
    assertEquals(0, logRepository.count());
  }

  @Test
  void shouldRollbackReaderUpdateWhenAuditFails() throws Exception{
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    when(logDescriptionBuilder.buildMessage(
            eq(LogEventType.READER_UPDATED),any(Reader.class),isNull(), any(ReaderDTO.class), isNull()
    )).thenThrow(new IllegalStateException("Simulated Exception"));
    String request = """
            {
              "middleName": "Max"
            }
            """;
    mockMvc.perform(patch("/api/readers/{id}", testedReader.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertInstanceOf(
                    IllegalStateException.class, result.getResolvedException()));
    Reader persistedReader = readerRepository.findById(testedReader.getId()).orElseThrow();
    assertNull(persistedReader.getMiddleName());
    assertEquals(1, readerRepository.count());
    assertEquals(0, logRepository.count());
  }

  @Test
  void shouldRollbackBorrowBookWhenAuditFails() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    when(logDescriptionBuilder.buildMessage(
            eq(LogEventType.BOOK_BORROWED),any(Reader.class),any(Book.class), isNull(), isNull()
    )).thenThrow(new IllegalStateException("Simulated Exception"));
    String request = """
            {
              "bookId": %d,
              "readerId": %d
            }
            """.formatted(testedBook.getId(), testedReader.getId());
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertInstanceOf(
                    IllegalStateException.class, result.getResolvedException()));
    assertEquals(0, borrowRepository.count());
    assertEquals(0, logRepository.count());
  }

  @Test
  void shouldRollbackReturnBookWhenAuditFails() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    Borrow borrow = borrowRepository.save(new Borrow(
            testedBook, testedReader, LocalDate.now(clock).minusDays(1)));
    when(logDescriptionBuilder.buildMessage(
            eq(LogEventType.BOOK_RETURNED),any(Reader.class),any(Book.class), isNull(), isNull()
    )).thenThrow(new IllegalStateException("Simulated Exception"));
    mockMvc.perform(patch("/api/borrows/{id}/return",borrow.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(result -> assertInstanceOf(
                    IllegalStateException.class, result.getResolvedException()));
    Borrow persistedBorrow = borrowRepository
            .findById(borrow.getId())
            .orElseThrow();
    assertNull(persistedBorrow.getReturnDate());
    assertEquals(1, borrowRepository.count());
    assertEquals(0, logRepository.count());
  }
}
