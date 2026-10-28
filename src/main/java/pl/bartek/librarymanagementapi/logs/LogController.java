package pl.bartek.librarymanagementapi.logs;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.web.PagedModel;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/logs")
public class LogController {

  private final LogService logService;

  public LogController(LogService logService) {
    this.logService = logService;
  }

  @GetMapping
  public PagedModel<LogDTO> getLogs(
          @RequestParam(defaultValue = "0") @Min(0) int page,
          @RequestParam(defaultValue = "30") @Min(5) @Max(100) int limit) {
    return new PagedModel<>(logService.getLogs(limit, page));
  }

  @GetMapping("/readers/{id}")
  public PagedModel<LogDTO> getLogsByReaderId(
          @PathVariable Integer id,
          @RequestParam(defaultValue = "0") @Min(0) int page,
          @RequestParam(defaultValue = "30") @Min(5) @Max(100) Integer limit
          ) {
    return new PagedModel<>(logService.getReaderLogs(limit, page, id));
  }

  @GetMapping("books/{id}")
  public PagedModel<LogDTO> getLogsByBookId(
          @PathVariable Integer id,
          @RequestParam(defaultValue = "0") @Min(0) int page,
          @RequestParam(defaultValue = "30") @Min(5) @Max(100) Integer limit
          ) {
    return new PagedModel<>(logService.getBookLogs(limit, page, id));
  }
}
