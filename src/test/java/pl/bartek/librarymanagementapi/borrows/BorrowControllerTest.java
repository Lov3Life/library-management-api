package pl.bartek.librarymanagementapi.borrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pl.bartek.librarymanagementapi.books.BookService;
import pl.bartek.librarymanagementapi.books.exceptions.BookNotFoundException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowAlreadyReturnedException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowConflictException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowNotFoundException;
import pl.bartek.librarymanagementapi.readers.ReaderService;
import pl.bartek.librarymanagementapi.readers.exceptions.ReaderNotFoundException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BorrowController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BorrowControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  private ReaderService readerService;

  @MockitoBean
  private BookService bookService;

  @MockitoBean
  private BorrowService borrowService;

  @Test
  void shouldReturn404WhenBorrowDoesNotExist() throws Exception {
    when(borrowService.getBorrow(anyInt()))
            .thenThrow(new BorrowNotFoundException(1));
    mockMvc.perform(get("/api/borrows/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Borrow with id 1 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
    verify(borrowService).getBorrow(anyInt());
  }

  @Test
  void shouldReturn200WhenBorrowExists() throws Exception {
    BorrowDTO borrowDTO = new BorrowDTO(1, 1, 1, LocalDate.of(2023, 1, 1), null);
    when(borrowService.getBorrow(anyInt()))
            .thenReturn(borrowDTO);
    mockMvc.perform(get("/api/borrows/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.readerId").value(1))
            .andExpect(jsonPath("$.bookId").value(1))
            .andExpect(jsonPath("$.borrowDate").value("2023-01-01"));
    verify(borrowService).getBorrow(anyInt());
  }

  @Test
  void shouldReturnAllBorrows() throws Exception {
    BorrowDTO borrowDTO = new BorrowDTO(1, 10, 5,
            LocalDate.of(2023, 1, 1), null);
    when(borrowService.getBorrows(null, null, null))
            .thenReturn(List.of(borrowDTO));
    mockMvc.perform(get("/api/borrows"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].bookId").value(10))
            .andExpect(jsonPath("$[0].readerId").value(5))
            .andExpect(jsonPath("$[0].borrowDate").value("2023-01-01"));
    verify(borrowService).getBorrows(null, null, null);
  }

  @ParameterizedTest
  @CsvSource(value = {
          "NULL, NULL, NULL",
          "NULL, NULL, true",
          "NULL, NULL, false",
          "1, NULL, NULL",
          "1, NULL, true",
          "1, NULL, false",
          "NULL, 10, NULL",
          "NULL, 10, true",
          "NULL, 10, false",
          "1, 10, NULL",
          "1, 10, true",
          "1, 10, false"
          }, nullValues = "NULL")
  void shouldReturn200AndPassFiltersToService(Integer readerId, Integer bookId, Boolean returned) throws Exception {
    when(borrowService.getBorrows(readerId, bookId, returned))
            .thenReturn(List.of());
    MockHttpServletRequestBuilder request = get("/api/borrows");
    if (readerId != null)
      request.param("readerId", readerId.toString());
    if (bookId != null)
      request.param("bookId", bookId.toString());
    if (returned != null)
      request.param("returned", returned.toString());
    mockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    verify(borrowService).getBorrows(readerId, bookId, returned);
  }

  @Test
  void shouldReturn201WhenCreateBorrowSuccessfully() throws Exception {
    String request = """
            {
              "bookId": 1,
              "readerId": 1
            }
            """;
    BorrowDTO response = new BorrowDTO(1, 1, 1, LocalDate.now(), null);
    when(borrowService.createBorrow(any()))
            .thenReturn(response);
    mockMvc.perform(post("/api/borrows")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isCreated())
            .andExpect(content().json("""
            {
              "id": 1,
              "bookId": 1,
              "readerId": 1,
              "borrowDate": "%s",
              "returnDate": null
            }
            """.formatted(response.borrowDate().toString())));
    verify(borrowService).createBorrow(any());
  }

  @Test
  void shouldReturn400WhenCreateBorrowAndNoBookId() throws Exception {
    String request = """
            {
              "readerId": 1
            }
            """;
    mockMvc.perform(post("/api/borrows")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenCreateBorrowAndNoReaderId() throws Exception {
    String request = """
            {
              "bookId": 1
            }
            """;
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenCreateBorrowAndNoReaderIdAndNoBookId() throws Exception {
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn404WhenCreateBorrowAndBookNotFound() throws Exception {
    String request = """
            {
              "bookId": 1,
              "readerId": 1
            }
            """;
    when(borrowService.createBorrow(any(BorrowCreateRequest.class)))
            .thenThrow(new BookNotFoundException(1));
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Book with id: 1 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn404WhenCreateBorrowAndReaderNotFound() throws Exception {
    String request = """
            {
              "bookId": 1,
              "readerId": 1
            }
            """;
    when(borrowService.createBorrow(any(BorrowCreateRequest.class)))
            .thenThrow(new ReaderNotFoundException(1));
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Reader with id: 1 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "book with id: 1 is already borrowed.",
          "book with id: 1 is not active.",
          "reader with id: 1 is not active."
  })
  void shouldReturn409WhenConflictInCreateRequest(String message) throws Exception {
    String request = """
            {
              "bookId": 1,
              "readerId": 1
            }
            """;
    when(borrowService.createBorrow(any(BorrowCreateRequest.class)))
            .thenThrow(new BorrowConflictException(message));
    mockMvc.perform(post("/api/borrows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value(message))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn404WhenReturnBookAndBorrowDoesNotExist() throws Exception {
    when(borrowService.returnBook(anyInt()))
            .thenThrow(new BorrowNotFoundException(1));
    mockMvc.perform(patch("/api/borrows/1/return")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Borrow with id 1 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
    verify(borrowService).returnBook(anyInt());
  }

  @Test
  void shouldReturn409WhenBookIsAlreadyReturned() throws Exception {
    when(borrowService.returnBook(anyInt()))
            .thenThrow(new BorrowAlreadyReturnedException(
                    "Borrow with id: 1 has already been returned."));
    mockMvc.perform(patch("/api/borrows/1/return")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Borrow with id: 1 has already been returned."))
            .andExpect(jsonPath("$.trace").doesNotExist());
    verify(borrowService).returnBook(anyInt());
  }

  @Test
  void shouldReturn200WhenBookReturnedSuccessfully() throws Exception {
    when(borrowService.returnBook(anyInt()))
            .thenReturn(new BorrowDTO(1, 1, 1,
                    LocalDate.of(2023, 1, 1), LocalDate.now()));
    mockMvc.perform(patch("/api/borrows/1/return")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.readerId").value(1))
            .andExpect(jsonPath("$.bookId").value(1))
            .andExpect(jsonPath("$.borrowDate").value("2023-01-01"))
            .andExpect(jsonPath("$.returnDate").value(LocalDate.now().toString()));
    verify(borrowService).returnBook(anyInt());
  }
}
