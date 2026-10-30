package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import pl.bartek.librarymanagementapi.logs.Log;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.logs.LogRepository;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class ReaderIntegrationTest {

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

  @BeforeEach
  void cleanDatabase() {
    logRepository.deleteAll();
    readerRepository.deleteAll();
  }

  @Test
  void shouldCreateReaderAndPersistAuditLog() throws Exception {
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
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.firstName").value("Edward"));
    List<Reader> books = readerRepository.findAll();
    assertEquals(1, books.size());
    Reader savedBook = books.getFirst();
    assertNotNull(savedBook.getId());
    assertEquals("Edward", savedBook.getFirstName());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log savedLog = logs.getFirst();
    assertEquals(LogEventType.READER_CREATED, savedLog.getEventType());
    assertNotNull(savedLog.getDate());
    assertTrue(savedLog.getDescription().contains("Edward"));
    assertEquals(1, logRepository.findByReaderId(savedBook.getId(),
            PageRequest.of(0, 10)).getTotalElements());
  }

  @Test
  void shouldUpdateReaderAndPersistAuditLog() throws Exception {
    Reader reader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    String request = """
                {
                  "middleName":"James"
                }
                """;
    mockMvc.perform(patch("/api/readers/{id}", reader.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.middleName").value("James"));
    List<Reader> readers = readerRepository.findAll();
    assertEquals(1, readers.size());
    Reader changedReader = readers.getFirst();
    assertNotNull(changedReader.getId());
    assertEquals("James", changedReader.getMiddleName());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log savedLog = logs.getFirst();
    assertEquals(LogEventType.READER_UPDATED, savedLog.getEventType());
    assertNotNull(savedLog.getDate());
    assertTrue(savedLog.getDescription().contains("null -> James"));
    assertEquals(1, logRepository.findByReaderId(changedReader.getId(),
            PageRequest.of(0, 10)).getTotalElements());
  }

  @Test
  void shouldUpdateReaderEmptyBodyAndNoAuditLog() throws Exception {
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    String request = """
                {
                }
                """;
    mockMvc.perform(patch("/api/readers/{id}", testedReader.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.firstName").value("John"));
    List<Reader> readers = readerRepository.findAll();
    assertEquals(1, readers.size());
    Reader changedReader = readers.getFirst();
    assertNotNull(changedReader.getId());
    assertEquals("John", changedReader.getFirstName());
    List<Log> logs = logRepository.findAll();
    assertEquals(0, logs.size());
  }

  @Test
  void shouldDeactivateReaderAndPersistAuditLog() throws Exception {
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    mockMvc.perform(patch("/api/readers/{id}/deactivate", testedReader.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.active").value(false));
    List<Reader> readers = readerRepository.findAll();
    assertEquals(1, readers.size());
    Reader changedReader = readers.getFirst();
    assertNotNull(changedReader.getId());
    assertEquals("John", changedReader.getFirstName());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
    Log log = logs.getFirst();
    assertEquals(LogEventType.READER_DEACTIVATED, log.getEventType());
    assertTrue(log.getDescription().contains("deactivated"));
    assertEquals(testedReader.getId(), log.getReader().getId());
  }

  @Test
  void shouldNotDeactivateAlreadyDeactivatedReader() throws Exception {
    Reader testedReader = readerRepository.save(new Reader(
            "John", null, "Smith", LocalDate.of(1998, 5, 21)));
    mockMvc.perform(patch("/api/readers/{id}/deactivate", testedReader.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.active").value(false));
    mockMvc.perform(patch("/api/readers/{id}/deactivate", testedReader.getId())
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
    List<Reader> readers = readerRepository.findAll();
    assertEquals(1, readers.size());
    Reader changedReader = readers.getFirst();
    assertNotNull(changedReader.getId());
    assertFalse(changedReader.isActive());
    List<Log> logs = logRepository.findAll();
    assertEquals(1, logs.size());
  }
}
