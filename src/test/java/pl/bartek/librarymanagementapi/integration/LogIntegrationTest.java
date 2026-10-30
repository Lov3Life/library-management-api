package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookRepository;
import pl.bartek.librarymanagementapi.integration.config.MySqlTestConfiguration;
import pl.bartek.librarymanagementapi.logs.Log;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.logs.LogRepository;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MySqlTestConfiguration.class)
public class LogIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private LogRepository logRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private ReaderRepository readerRepository;

  @BeforeEach
  void setUp() {
    logRepository.deleteAll();
    bookRepository.deleteAll();
    readerRepository.deleteAll();
  }

  @Test
  void shouldFilterLogsByBookId() throws Exception {
    Book book1 = bookRepository.save(new Book("The Hobbit", null, 1937, null));
    Book book2 = bookRepository.save(new Book("1984", null, 1949, null));
    LocalDateTime date = LocalDateTime.of(2022, 2, 2, 2, 2);
    logRepository.save(new Log(date, LogEventType.BOOK_CREATED, "First book", null, book1));
    logRepository.save(new Log(date, LogEventType.BOOK_CREATED, "Second book", null, book2));
    logRepository.save(new Log(date, LogEventType.BOOK_UPDATED, "First book updated", null, book1));
    mockMvc.perform(get("/api/logs/books/{id}", book1.getId())
                    .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(2))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].bookId").value(book1.getId()))
            .andExpect(jsonPath("$.content[1].bookId").value(book1.getId()));
  }

  @Test
  void shouldFilterLogsByReaderId() throws Exception {
    Reader reader1 = readerRepository.save(new Reader("John", null,
            "Smith", LocalDate.of(1998, 5, 21)));
    Reader reader2 = readerRepository.save(new Reader("John", null,
            "Smith", LocalDate.of(1998, 5, 21)));
    LocalDateTime date = LocalDateTime.of(2022, 2, 2, 2, 2);
    logRepository.save(new Log(date, LogEventType.READER_CREATED, "First book", reader1, null));
    logRepository.save(new Log(date, LogEventType.READER_CREATED, "Second book", reader2, null));
    logRepository.save(new Log(date, LogEventType.READER_UPDATED, "First book updated", reader1, null));
    mockMvc.perform(get("/api/logs/readers/{id}", reader1.getId())
                    .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(2))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].readerId").value(reader1.getId()))
            .andExpect(jsonPath("$.content[1].readerId").value(reader1.getId()));
  }

  @Test
  void shouldSortLogsByDateAndIdDesc() throws Exception {
    LocalDateTime older = LocalDateTime.of(2022, 2, 2, 2, 2);
    LocalDateTime newer = older.plusDays(1);
    Log log1 = logRepository.save(new Log(older, LogEventType.READER_CREATED, "First book", null, null));
    Log log2 = logRepository.save(new Log(newer, LogEventType.READER_CREATED, "Second book", null, null));
    Log log3 = logRepository.save(new Log(newer, LogEventType.READER_UPDATED, "Second book", null, null));
    mockMvc.perform(get("/api/logs")
                    .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(3))
            .andExpect(jsonPath("$.content[0].id").value(log3.getId()))
            .andExpect(jsonPath("$.content[1].id").value(log2.getId()))
            .andExpect(jsonPath("$.content[2].id").value(log1.getId()));
  }

  @Test
  void shouldPageNumbering() throws Exception {
    LocalDateTime older = LocalDateTime.of(2022, 2, 2, 2, 2);
    LocalDateTime newer = older.plusDays(1);
    Log log1 = logRepository.save(new Log(older, LogEventType.READER_CREATED, "First book", null, null));
    Log log2 = logRepository.save(new Log(older, LogEventType.READER_CREATED, "Second book", null, null));
    Log log3 = logRepository.save(new Log(older, LogEventType.READER_UPDATED, "Second book", null, null));
    Log log4 = logRepository.save(new Log(newer, LogEventType.READER_CREATED, "First book", null, null));
    Log log5 = logRepository.save(new Log(newer, LogEventType.READER_CREATED, "Second book", null, null));
    Log log6 = logRepository.save(new Log(newer, LogEventType.READER_UPDATED, "Second book", null, null));
    mockMvc.perform(get("/api/logs")
                    .param("limit", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(6))
            .andExpect(jsonPath("$.page.totalPages").value(2))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.content[0].id").value(log6.getId()))
            .andExpect(jsonPath("$.content[1].id").value(log5.getId()))
            .andExpect(jsonPath("$.content[2].id").value(log4.getId()))
            .andExpect(jsonPath("$.content[3].id").value(log3.getId()))
            .andExpect(jsonPath("$.content[4].id").value(log2.getId()));
    mockMvc.perform(get("/api/logs")
                    .param("limit", "5")
                    .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(6))
            .andExpect(jsonPath("$.page.totalPages").value(2))
            .andExpect(jsonPath("$.page.number").value(1))
            .andExpect(jsonPath("$.content[0].id").value(log1.getId()));
  }
}
