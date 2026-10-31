package pl.bartek.librarymanagementapi.logs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogController.class)
@AutoConfigureMockMvc(addFilters = false)
class LogControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private LogService logService;

  @Test
  void shouldReturn200WithPagedLogs() throws Exception {
    LogDTO logDTO = new LogDTO(1, LocalDateTime.of(2026, 10, 28, 12, 0),
            LogEventType.BOOK_CREATED, "Book created.", null, 5);
    Page<LogDTO> page = new PageImpl<>(List.of(logDTO), PageRequest.of(0, 30), 1);
    when(logService.getLogs(30, 0))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].eventType").value("BOOK_CREATED"))
            .andExpect(jsonPath("$.content[0].bookId").value(5))
            .andExpect(jsonPath("$.page.size").value(30))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.totalElements").value(1))
            .andExpect(jsonPath("$.page.totalPages").value(1));
    verify(logService).getLogs(30, 0);
  }

  @Test
  void shouldReturn200WithEmptyContent() throws Exception {
    Page<LogDTO> page = Page.empty(PageRequest.of(0, 30));
    when(logService.getLogs(30, 0))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content").isEmpty())
            .andExpect(jsonPath("$.page.size").value(30))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.totalElements").value(0))
            .andExpect(jsonPath("$.page.totalPages").value(0));
    verify(logService).getLogs(30, 0);
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1", "0", "4", "101", "200"})
  void shouldReturn400WhenLimitIsInvalid(String limit) throws Exception {
    mockMvc.perform(get("/api/logs")
            .param("limit", limit))
            .andExpect(status().isBadRequest());
    verifyNoInteractions(logService);
  }

  @Test
  void shouldPassCustomPaginationToService() throws Exception {
    Page<LogDTO> page = Page.empty(PageRequest.of(1, 5));
    when(logService.getLogs(5, 1))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs")
            .param("limit", "5")
            .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(1))
            .andExpect(jsonPath("$.page.size").value(5));
    verify(logService).getLogs(5, 1);
  }

  @Test
  void shouldReturn200WithReaderLogs() throws Exception {
    LogDTO logDTO = new LogDTO(1, LocalDateTime.of(2026, 10, 28, 12, 0),
            LogEventType.READER_CREATED, "Reader created.", 5, null);
    Page<LogDTO> page = new PageImpl<>(List.of(logDTO), PageRequest.of(0, 30), 1);
    when(logService.getReaderLogs(30, 0, 5))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs/readers/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].eventType").value("READER_CREATED"))
            .andExpect(jsonPath("$.content[0].readerId").value(5))
            .andExpect(jsonPath("$.page.size").value(30))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.totalElements").value(1))
            .andExpect(jsonPath("$.page.totalPages").value(1));
    verify(logService).getReaderLogs(30, 0, 5);
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1", "0", "4", "101", "200"})
  void shouldReturn400WhenLimitIsInvalidGetReaderLogs(String limit) throws Exception {
    mockMvc.perform(get("/api/logs/readers/5")
                    .param("limit", limit))
            .andExpect(status().isBadRequest());
    verifyNoInteractions(logService);
  }

  @ParameterizedTest
  @ValueSource(strings = {"5", "6", "50", "99", "100"})
  void shouldReturn200WhenValidGetReaderLogs(String limit) throws Exception {
    Page<LogDTO> page = Page.empty(PageRequest.of(0, Integer.parseInt(limit)));
    when(logService.getReaderLogs(Integer.parseInt(limit), 0, 5))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs/readers/5")
                    .param("limit", limit))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.size").value(Integer.parseInt(limit)));
    verify(logService).getReaderLogs(Integer.parseInt(limit), 0, 5);
  }

  @Test
  void shouldPassCustomPaginationToServiceReaderLogs() throws Exception {
    Page<LogDTO> page = Page.empty(PageRequest.of(1, 5));
    when(logService.getReaderLogs(5, 1, 5))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs/readers/5")
                    .param("limit", "5")
                    .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(1))
            .andExpect(jsonPath("$.page.size").value(5));
    verify(logService).getReaderLogs(5, 1, 5);
  }

  @Test
  void shouldReturn200WithBookLogs() throws Exception {
    LogDTO logDTO = new LogDTO(1, LocalDateTime.of(2026, 10, 28, 12, 0),
            LogEventType.BOOK_ACTIVATED, "Book Activated.", null, 5);
    Page<LogDTO> page = new PageImpl<>(List.of(logDTO), PageRequest.of(0, 30), 1);
    when(logService.getBookLogs(30, 0, 5))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs/books/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].eventType").value("BOOK_ACTIVATED"))
            .andExpect(jsonPath("$.content[0].bookId").value(5))
            .andExpect(jsonPath("$.page.size").value(30))
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.totalElements").value(1))
            .andExpect(jsonPath("$.page.totalPages").value(1));
    verify(logService).getBookLogs(30, 0, 5);
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1", "0", "4", "101", "200"})
  void shouldReturn400WhenLimitIsInvalidGetBookLogs(String limit) throws Exception {
    mockMvc.perform(get("/api/logs/books/5")
                    .param("limit", limit))
            .andExpect(status().isBadRequest());
    verifyNoInteractions(logService);
  }

  @ParameterizedTest
  @ValueSource(strings = {"5", "6", "50", "99", "100"})
  void shouldReturn200WhenValidGetBookLogs(String limit) throws Exception {
    Page<LogDTO> page = Page.empty(PageRequest.of(0, Integer.parseInt(limit)));
    when(logService.getBookLogs(Integer.parseInt(limit), 0, 5))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs/books/5")
                    .param("limit", limit))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(0))
            .andExpect(jsonPath("$.page.size").value(Integer.parseInt(limit)));
    verify(logService).getBookLogs(Integer.parseInt(limit), 0, 5);
  }

  @Test
  void shouldPassCustomPaginationToServiceBookLogs() throws Exception {
    Page<LogDTO> page = Page.empty(PageRequest.of(1, 5));
    when(logService.getBookLogs(5, 1, 5))
            .thenReturn(page);
    mockMvc.perform(get("/api/logs/books/5")
                    .param("limit", "5")
                    .param("page", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.number").value(1))
            .andExpect(jsonPath("$.page.size").value(5));
    verify(logService).getBookLogs(5, 1, 5);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/logs", "/api/logs/readers/5", "/api/logs/books/5"})
  void shouldReturn400WhenPageIsInvalid(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .param("page", "-1"))
            .andExpect(status().isBadRequest());
    verifyNoInteractions(logService);
  }
}