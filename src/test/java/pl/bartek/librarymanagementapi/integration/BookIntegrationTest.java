package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookRepository;
import pl.bartek.librarymanagementapi.integration.config.MySqlTestConfiguration;
import pl.bartek.librarymanagementapi.logs.Log;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.logs.LogRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Import(MySqlTestConfiguration.class)
public class BookIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private LogRepository logRepository;

  @BeforeEach
  void cleanDatabase() {
    logRepository.deleteAll();
    bookRepository.deleteAll();
  }

  @Test
  void shouldCreateBookAndPersistAuditLog() throws Exception {
    String request = """
                {
                  "title": "The Hobbit",
                  "author": "J.R.R. Tolkien",
                  "releaseYear": 1937,
                  "publishingHouse": "George Allen & Unwin"
                }
                """;
    mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("The Hobbit"));
    List<Book> books = bookRepository.findAll();
    assertEquals(1, books.size());
    Book savedBook = books.getFirst();
    assertNotNull(savedBook.getId());
    assertEquals("The Hobbit", savedBook.getTitle());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log savedLog = logs.getFirst();
    assertEquals(LogEventType.BOOK_CREATED, savedLog.getEventType());
    assertNotNull(savedLog.getDate());
    assertTrue(savedLog.getDescription().contains("The Hobbit"));
    assertEquals(1, logRepository.findByBookId(savedBook.getId(),
            PageRequest.of(0, 10)).getTotalElements());
  }

  @Test
  void shouldUpdateBookAndPersistAuditLog() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "Lord of the Rings", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    String request = """
                {
                  "title":"The Hobbit"
                }
                """;
    mockMvc.perform(patch("/api/books/{id}", testedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("The Hobbit"));
    List<Book> books = bookRepository.findAll();
    assertEquals(1, books.size());
    Book changedBook = books.getFirst();
    assertNotNull(changedBook.getId());
    assertEquals("The Hobbit", changedBook.getTitle());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log savedLog = logs.getFirst();
    assertEquals(LogEventType.BOOK_UPDATED, savedLog.getEventType());
    assertNotNull(savedLog.getDate());
    assertTrue(savedLog.getDescription().contains("Lord of the Rings -> The Hobbit"));
    assertEquals(1, logRepository.findByBookId(changedBook.getId(),
            PageRequest.of(0, 10)).getTotalElements());
  }

  @Test
  void shouldUpdateBookEmptyBodyAndNoAuditLog() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "Lord of the Rings", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    String request = """
                {
                }
                """;
    mockMvc.perform(patch("/api/books/{id}", testedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.title").value("Lord of the Rings"));
    List<Book> books = bookRepository.findAll();
    assertEquals(1, books.size());
    Book changedBook = books.getFirst();
    assertNotNull(changedBook.getId());
    assertEquals("Lord of the Rings", changedBook.getTitle());
    List<Log> logs = logRepository.findAll();
    assertEquals(0, logs.size());
  }

  @Test
  void shouldDeactivateBookAndPersistAuditLog() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    mockMvc.perform(patch("/api/books/{id}/deactivate", testedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.active").value(false));
    List<Book> books = bookRepository.findAll();
    assertEquals(1, books.size());
    Book changedBook = books.getFirst();
    assertNotNull(changedBook.getId());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log log = logs.getFirst();
    assertEquals(LogEventType.BOOK_DEACTIVATED, log.getEventType());
    assertTrue(log.getDescription().contains("deactivated"));
    assertEquals(testedBook.getId(), log.getBook().getId());
  }

  @Test
  void shouldNotDeactivateAlreadyDeactivatedBook() throws Exception {
    Book testedBook = bookRepository.save(new Book(
            "The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin"));
    mockMvc.perform(patch("/api/books/{id}/deactivate", testedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.active").value(false));
    mockMvc.perform(patch("/api/books/{id}/deactivate", testedBook.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    List<Book> books = bookRepository.findAll();
    assertEquals(1, books.size());
    Book changedBook = books.getFirst();
    assertNotNull(changedBook.getId());
    assertFalse(changedBook.isActive());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
  }
}
