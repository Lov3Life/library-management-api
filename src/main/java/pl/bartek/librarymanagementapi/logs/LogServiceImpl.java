package pl.bartek.librarymanagementapi.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookDTO;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderDTO;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class LogServiceImpl implements LogService {

  private final LogRepository logRepository;
  private final Clock clock;
  private final LogDescriptionBuilder logDescriptionBuilder;

  public LogServiceImpl(LogRepository logRepository, Clock clock, LogDescriptionBuilder logDescriptionBuilder) {
    this.logRepository = logRepository;
    this.clock = clock;
    this.logDescriptionBuilder = logDescriptionBuilder;
  }

  @Override
  public void logEvent(LogEventType logEventType, Reader reader, Book book) {
    logEvent(logEventType, reader, book, null, null);
  }

  @Override
  public void logEvent(LogEventType logEventType,
                       Reader reader, Book book,
                       ReaderDTO readerPrev, BookDTO bookPrev) {
    String massage = logDescriptionBuilder.buildMessage(logEventType, reader, book, readerPrev, bookPrev);
    Log log = new Log(LocalDateTime.now(clock), logEventType, massage, reader, book);
    logRepository.save(log);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LogDTO> getLogs(Integer limit, Integer page) {
    Pageable pageable = createPageable(page, limit);
    return logRepository.findAll(pageable)
            .map(LogDTO::fromLog);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LogDTO> getReaderLogs(Integer limit, Integer page, Integer readerId) {
    Pageable pageable = createPageable(page, limit);
    return logRepository.findByReaderId(readerId, pageable)
            .map(LogDTO::fromLog);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LogDTO> getBookLogs(Integer limit, Integer page, Integer bookId) {
    Pageable pageable = createPageable(page, limit);
    return logRepository.findByBookId(bookId, pageable)
            .map(LogDTO::fromLog);
  }

  private Pageable createPageable(int page, int limit) {
    Sort sort = Sort.by(Sort.Order.desc("date"),
            Sort.Order.desc("id"));

    return PageRequest.of(page, limit, sort);
  }
}
