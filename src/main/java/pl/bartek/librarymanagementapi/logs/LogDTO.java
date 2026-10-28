package pl.bartek.librarymanagementapi.logs;

import java.time.LocalDateTime;

public record LogDTO(Integer id,
                     LocalDateTime date,
                     LogEventType eventType,
                     String description,
                     Integer readerId,
                     Integer bookId) {
  public static LogDTO fromLog(Log log) {
    return new LogDTO(log.getId(), log.getDate(),
            log.getEventType(), log.getDescription(),
            log.getReader() != null ? log.getReader().getId() : null,
            log.getBook() != null ? log.getBook().getId() : null);
  }
}
