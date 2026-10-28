package pl.bartek.librarymanagementapi.logs;

import org.springframework.data.domain.Page;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookDTO;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderDTO;

public interface LogService {

  void logEvent(LogEventType logEventType,
                Reader reader, Book book);

  void logEvent(LogEventType logEventType,
                Reader reader, Book book,
                ReaderDTO readerPrev, BookDTO bookPrev);

  Page<LogDTO> getLogs(Integer limit, Integer page);

  Page<LogDTO> getReaderLogs(Integer limit, Integer page, Integer readerId);

  Page<LogDTO> getBookLogs(Integer limit, Integer page, Integer bookId);
}
