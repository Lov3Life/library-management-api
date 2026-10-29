package pl.bartek.librarymanagementapi.logs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookDTO;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderDTO;

import java.sql.Date;
import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.bartek.librarymanagementapi.logs.LogEventType.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

  @Mock
  private LogRepository logRepository;

  @Mock
  private LogDescriptionBuilder logDescriptionBuilder;

  private LogServiceImpl logService;

  private static final Clock FIXED_CLOCK = Clock.fixed(
          Instant.parse("2022-01-01T00:00:00Z"),
          ZoneId.of("Europe/Warsaw"));

  @BeforeEach
  void setUp() {
    logService = new LogServiceImpl(logRepository, FIXED_CLOCK, logDescriptionBuilder);
  }

  @Test
  void shouldSaveBookCreatedLog() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
    String description = "Book The Hobbit created.";
    when(logDescriptionBuilder.buildMessage(BOOK_CREATED, null, book, null, null))
            .thenReturn(description);
    logService.logEvent(BOOK_CREATED, null, book);
    ArgumentCaptor<Log> argumentCaptor = ArgumentCaptor.forClass(Log.class);
    verify(logRepository).save(argumentCaptor.capture());
    Log log = argumentCaptor.getValue();
    assertEquals(BOOK_CREATED, log.getEventType());
    assertEquals(description, log.getDescription());
    assertEquals(LocalDateTime.now(FIXED_CLOCK), log.getDate());
    assertSame(book, log.getBook());
    assertNull(log.getReader());
  }

  @Test
  void shouldSaveReaderCreatedLog() {
    Reader reader = new Reader("John", "James", "Smith", Date.valueOf("1998-05-21").toLocalDate());
    String description = "Reader John James Smith created.";
    when(logDescriptionBuilder.buildMessage(READER_CREATED, reader, null, null, null))
            .thenReturn(description);
    logService.logEvent(READER_CREATED, reader, null);
    ArgumentCaptor<Log> argumentCaptor = ArgumentCaptor.forClass(Log.class);
    verify(logRepository).save(argumentCaptor.capture());
    Log log = argumentCaptor.getValue();
    assertEquals(READER_CREATED, log.getEventType());
    assertEquals(description, log.getDescription());
    assertEquals(LocalDateTime.now(FIXED_CLOCK), log.getDate());
    assertSame(reader, log.getReader());
    assertNull(log.getBook());
  }

  @Test
  void shouldSaveBorrowLog() {
    Reader reader = new Reader("John", "James", "Smith", Date.valueOf("1998-05-21").toLocalDate());
    Book book = new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
    String description = "Book borrowed.";
    when(logDescriptionBuilder.buildMessage(BOOK_BORROWED, reader, book, null, null))
            .thenReturn(description);
    logService.logEvent(BOOK_BORROWED, reader, book);
    ArgumentCaptor<Log> argumentCaptor = ArgumentCaptor.forClass(Log.class);
    verify(logRepository).save(argumentCaptor.capture());
    Log log = argumentCaptor.getValue();
    assertEquals(BOOK_BORROWED, log.getEventType());
    assertEquals(description, log.getDescription());
    assertEquals(LocalDateTime.now(FIXED_CLOCK), log.getDate());
    assertSame(reader, log.getReader());
    assertSame(book, log.getBook());
  }

  @Test
  void shouldSaveBookUpdateLog() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
    String description = "Book updated.";
    BookDTO previous = new BookDTO(1, "The Hobbit", null,
            1937, "George Allen & Unwin", true);
    when(logDescriptionBuilder.buildMessage(BOOK_UPDATED, null, book, null, previous))
            .thenReturn(description);
    logService.logEvent(BOOK_UPDATED, null, book, null, previous);
    ArgumentCaptor<Log> argumentCaptor = ArgumentCaptor.forClass(Log.class);
    verify(logRepository).save(argumentCaptor.capture());
    Log log = argumentCaptor.getValue();
    assertEquals(BOOK_UPDATED, log.getEventType());
    assertEquals(description, log.getDescription());
    assertEquals(LocalDateTime.now(FIXED_CLOCK), log.getDate());
    assertNull(log.getReader());
    assertSame(book, log.getBook());
  }

  @Test
  void shouldSaveReaderUpdateLog() {
    Reader reader = new Reader("John", "James", "Smith", Date.valueOf("1998-05-21").toLocalDate());
    String description = "Reader updated.";
    ReaderDTO previous = new ReaderDTO(1, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(logDescriptionBuilder.buildMessage(READER_UPDATED, reader, null, previous, null))
            .thenReturn(description);
    logService.logEvent(READER_UPDATED, reader, null, previous, null);
    ArgumentCaptor<Log> argumentCaptor = ArgumentCaptor.forClass(Log.class);
    verify(logRepository).save(argumentCaptor.capture());
    Log log = argumentCaptor.getValue();
    assertEquals(READER_UPDATED, log.getEventType());
    assertEquals(description, log.getDescription());
    assertEquals(LocalDateTime.now(FIXED_CLOCK), log.getDate());
    assertSame(reader, log.getReader());
    assertNull(log.getBook());
  }

  @Test
  void shouldReturnBookLogsWithCorrectPagination() {
    Book book = new Book();
    ReflectionTestUtils.setField(book, "id", 1);
    LocalDateTime date = LocalDateTime.now(FIXED_CLOCK);
    Log log = new Log(date, BOOK_UPDATED, "Book update", null, book);
    ReflectionTestUtils.setField(log, "id", 1);
    Sort sort = Sort.by(Sort.Order.desc("date"),
            Sort.Order.desc("id"));
    Pageable pageable = PageRequest.of(1, 5, sort);
    Page<Log> repositoryResult = new PageImpl<>(List.of(log), pageable, 11);
    when(logRepository.findByBookId(1, pageable))
            .thenReturn(repositoryResult);
    Page<LogDTO> result = logService.getBookLogs(5, 1, 1);
    assertEquals(11, result.getTotalElements());
    assertEquals(1, result.getNumber());
    assertEquals(5, result.getSize());
    LogDTO logDTO = result.getContent().getFirst();
    assertEquals(1, logDTO.id());
    assertEquals(BOOK_UPDATED, logDTO.eventType());
    assertEquals("Book update", logDTO.description());
    assertEquals(date, logDTO.date());
    assertSame(book.getId(), logDTO.bookId());
    assertNull(logDTO.readerId());
    verify(logRepository).findByBookId(1, pageable);
  }

  @Test
  void shouldReturnReaderLogsWithCorrectPagination() {
    Reader reader = new Reader();
    ReflectionTestUtils.setField(reader, "id", 1);
    LocalDateTime date = LocalDateTime.now(FIXED_CLOCK);
    Log log = new Log(date, READER_UPDATED, "Reader update", reader, null);
    ReflectionTestUtils.setField(log, "id", 1);
    Sort sort = Sort.by(Sort.Order.desc("date"),
            Sort.Order.desc("id"));
    Pageable pageable = PageRequest.of(1, 5, sort);
    Page<Log> repositoryResult = new PageImpl<>(List.of(log), pageable, 11);
    when(logRepository.findByReaderId(1, pageable))
            .thenReturn(repositoryResult);
    Page<LogDTO> result = logService.getReaderLogs(5, 1, 1);
    assertEquals(11, result.getTotalElements());
    assertEquals(1, result.getNumber());
    assertEquals(5, result.getSize());
    LogDTO logDTO = result.getContent().getFirst();
    assertEquals(1, logDTO.id());
    assertEquals(READER_UPDATED, logDTO.eventType());
    assertEquals("Reader update", logDTO.description());
    assertEquals(date, logDTO.date());
    assertSame(reader.getId(), logDTO.readerId());
    assertNull(logDTO.bookId());
    verify(logRepository).findByReaderId(1, pageable);
  }

  @Test
  void shouldReturnAllLogs(){
    Book book = new Book();
    ReflectionTestUtils.setField(book, "id", 1);
    Reader reader = new Reader();
    ReflectionTestUtils.setField(reader, "id", 1);
    LocalDateTime date = LocalDateTime.now(FIXED_CLOCK);
    Log log1 = new Log(date, READER_UPDATED, "Reader update", reader, null);
    ReflectionTestUtils.setField(log1, "id", 1);
    Log log2 = new Log(date, BOOK_UPDATED, "Book update", null, book);
    ReflectionTestUtils.setField(log2, "id", 2);
    Sort sort = Sort.by(Sort.Order.desc("date"),
            Sort.Order.desc("id"));
    Pageable pageable = PageRequest.of(1, 5, sort);
    Page<Log> repositoryResult = new PageImpl<>(List.of(log1, log2), pageable, 11);
    when(logRepository.findAll(pageable))
            .thenReturn(repositoryResult);
    Page<LogDTO> result = logService.getLogs(5, 1);
    assertEquals(11, result.getTotalElements());
    assertEquals(1, result.getNumber());
    assertEquals(5, result.getSize());
    LogDTO logDTO1 = result.getContent().getFirst();
    LogDTO logDTO2 = result.getContent().get(1);
    assertNull(logDTO1.bookId());
    assertNull(logDTO2.readerId());
    verify(logRepository).findAll(pageable);
  }
}