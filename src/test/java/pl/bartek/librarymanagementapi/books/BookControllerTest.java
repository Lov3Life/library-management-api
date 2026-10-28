package pl.bartek.librarymanagementapi.books;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.books.exceptions.*;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
public class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private BookService bookService;

  @Test
  void shouldReturn404WhenBookDoesNotExist() throws Exception {
    when(bookService.getBook(999))
            .thenThrow(new BookNotFoundException(999));
    mockMvc.perform(get("/api/books/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Book with id: 999 not found"))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn200WhenBookExists() throws Exception {
    BookDTO bookDTO = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);
    when(bookService.getBook(1))
            .thenReturn(bookDTO);
    mockMvc.perform(get("/api/books/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("The Hobbit"))
            .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$.releaseYear").value(1937))
            .andExpect(jsonPath("$.publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$.active").value(true));

    verify(bookService).getBook(1);
  }

  @Test
  void shouldReturn200WhenAllBooks() throws Exception {
    BookDTO book1 = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);
    BookDTO book2 = new BookDTO(2, "The Lord of the Rings", "J.R.R. Tolkien",
            1954, "George Allen & Unwin", true);
    when(bookService.getBooks(null, null))
            .thenReturn(List.of(book1, book2));
    mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("The Hobbit"))
            .andExpect(jsonPath("$[0].author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$[0].releaseYear").value(1937))
            .andExpect(jsonPath("$[0].publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$[0].active").value(true))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].title").value("The Lord of the Rings"))
            .andExpect(jsonPath("$[1].author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$[1].releaseYear").value(1954))
            .andExpect(jsonPath("$[1].publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$[1].active").value(true));

    verify(bookService).getBooks(null, null);
  }

  @Test
  void shouldReturn200WhenEmptyArray() throws Exception {
    when(bookService.getBooks(null, null))
            .thenReturn(List.of());
    mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    verify(bookService).getBooks(null, null);
  }

  @Test
  void shouldReturn200WhenBookWithMatchedTitle() throws Exception {
    BookDTO book = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);
    when(bookService.getBooks("Hobbit", null))
            .thenReturn(List.of(book));
    mockMvc.perform(get("/api/books")
                    .param("title", "Hobbit"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("The Hobbit"))
            .andExpect(jsonPath("$[0].author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$[0].releaseYear").value(1937))
            .andExpect(jsonPath("$[0].publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$[0].active").value(true));
    verify(bookService).getBooks("Hobbit", null);
  }

  @Test
  void shouldReturn200WhenBookWithActiveTrue() throws Exception {
    BookDTO book = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);
    when(bookService.getBooks(null, true))
            .thenReturn(List.of(book));
    mockMvc.perform(get("/api/books")
                    .param("active", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("The Hobbit"))
            .andExpect(jsonPath("$[0].author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$[0].releaseYear").value(1937))
            .andExpect(jsonPath("$[0].publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$[0].active").value(true));
    verify(bookService).getBooks(null, true);
  }

  @Test
  void shouldReturn200WhenBookWithMatchedTitleAndActiveTrue() throws Exception {
    BookDTO book = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);
    when(bookService.getBooks("Hobbit", true))
            .thenReturn(List.of(book));
    mockMvc.perform(get("/api/books")
                    .param("title", "Hobbit")
                    .param("active", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].title").value("The Hobbit"))
            .andExpect(jsonPath("$[0].author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$[0].releaseYear").value(1937))
            .andExpect(jsonPath("$[0].publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$[0].active").value(true));
    verify(bookService).getBooks("Hobbit", true);
  }

  @Test
  void shouldReturn201WhenBookCreatedSuccessfully() throws Exception {
    BookCreateRequest bookCreateRequest = new BookCreateRequest("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    when(bookService.saveBook(bookCreateRequest)).thenReturn(new BookDTO(
            1, bookCreateRequest.title(),
            bookCreateRequest.author(), bookCreateRequest.releaseYear(),
            bookCreateRequest.publishingHouse(), true));
    mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookCreateRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("The Hobbit"))
            .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$.releaseYear").value(1937))
            .andExpect(jsonPath("$.publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$.active").value(true));
    verify(bookService).saveBook(bookCreateRequest);
  }

  @Test
  void shouldReturn201WhenBookCreatedSuccessfullyWithTitleAndNullValues() throws Exception {
    BookCreateRequest bookCreateRequest = new BookCreateRequest("The Hobbit", null,
            null, null);
    when(bookService.saveBook(bookCreateRequest)).thenReturn(new BookDTO(
            1, bookCreateRequest.title(),
            bookCreateRequest.author(), bookCreateRequest.releaseYear(),
            bookCreateRequest.publishingHouse(), true));
    mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookCreateRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("The Hobbit"))
            .andExpect(jsonPath("$.author").value(is(nullValue())))
            .andExpect(jsonPath("$.releaseYear").value(is(nullValue())))
            .andExpect(jsonPath("$.publishingHouse").value(is(nullValue())))
            .andExpect(jsonPath("$.active").value(true));
    verify(bookService).saveBook(bookCreateRequest);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "    ", "\t", "\n"})
  void shouldReturn400WhenBookCreateRequestHasInvalidTitle(String title) throws Exception {
    BookCreateRequest bookCreateRequest = new BookCreateRequest(title, null, null, null);
    mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookCreateRequest)))
            .andExpect(status().isBadRequest());
    verify(bookService, never()).saveBook(any(BookCreateRequest.class));
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {" ", "    ", "\t", "\n"})
  void shouldReturn400WhenBookUpdateRequestHasInvalidTitleHandlingByNotBlankInBookCreateRequestRecord(String title) throws Exception {
    String request = objectMapper.writeValueAsString(Map.of("title", title));
    mockMvc.perform(patch("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    verify(bookService, never()).updateBook(eq(1), any(BookUpdateRequest.class));
  }

  @Test
  void shouldReturn400WhenBookUpdatingRequestHasNullTitle() throws Exception {
    when(bookService.updateBook(eq(1), any(BookUpdateRequest.class)))
            .thenThrow(new BookUpdateInvalidParameterException(
                    "Book title cannot be null or blank. If change is no needed, don't fill param"));
    String request = """
            {
              "title": null
            }
            """;
    mockMvc.perform(patch("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail")
                    .value("Book title cannot be null or blank. If change is no needed, don't fill param"));
    ArgumentCaptor<BookUpdateRequest> captor = ArgumentCaptor.forClass(BookUpdateRequest.class);
    verify(bookService).updateBook(eq(1), captor.capture());
    BookUpdateRequest capturedRequest = captor.getValue();
    assertEquals(JsonNullable.of(null), capturedRequest.title());
  }

  @Test
  void shouldReturn200WhenBookUpdatingRequestHasEmptyBody() throws Exception {
    mockMvc.perform(patch("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
            .andExpect(status().isOk());
    ArgumentCaptor<BookUpdateRequest> captor = ArgumentCaptor.forClass(BookUpdateRequest.class);
    verify(bookService).updateBook(eq(1), captor.capture());
    BookUpdateRequest capturedRequest = captor.getValue();
    assertEquals(JsonNullable.undefined(), capturedRequest.title());
    assertEquals(JsonNullable.undefined(), capturedRequest.author());
    assertEquals(JsonNullable.undefined(), capturedRequest.releaseYear());
    assertEquals(JsonNullable.undefined(), capturedRequest.publishingHouse());
  }

  @Test
  void shouldReturn200WhenUpdatingTitle() throws Exception {
    BookDTO bookDTO = new BookDTO(1, "Lord of the Rings", "J.R.R. Tolkien",
            1954, "George Allen & Unwin", true);
    when(bookService.updateBook(eq(1), any(BookUpdateRequest.class)))
            .thenReturn(bookDTO);
    String request = """
            {
                "title": "Lord of the Rings"
            }
            """;
    mockMvc.perform(patch("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Lord of the Rings"))
            .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$.releaseYear").value(1954))
            .andExpect(jsonPath("$.publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$.active").value(true));

    ArgumentCaptor<BookUpdateRequest> captor = ArgumentCaptor.forClass(BookUpdateRequest.class);
    verify(bookService).updateBook(eq(1), captor.capture());
    BookUpdateRequest capturedRequest = captor.getValue();
    assertEquals("Lord of the Rings", capturedRequest.title().get());
    assertEquals(JsonNullable.undefined(), capturedRequest.author());
    assertEquals(JsonNullable.undefined(), capturedRequest.releaseYear());
    assertEquals(JsonNullable.undefined(), capturedRequest.publishingHouse());
  }

  @Test
  void shouldReturn200WhenUpdatingAllAvailableFields() throws Exception {
    BookDTO bookDTO = new BookDTO(1, "Lord of the Rings", "J.R.R. Tolkien",
            1954, "George Allen & Unwin", true);
    when(bookService.updateBook(eq(1), any(BookUpdateRequest.class)))
            .thenReturn(bookDTO);
    String request = """
            {
                "title": "Lord of the Rings",
                "author": "J.R.R. Tolkien",
                "releaseYear": 1954,
                "publishingHouse": "George Allen & Unwin"
            }
            """;
    mockMvc.perform(patch("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Lord of the Rings"))
            .andExpect(jsonPath("$.author").value("J.R.R. Tolkien"))
            .andExpect(jsonPath("$.releaseYear").value(1954))
            .andExpect(jsonPath("$.publishingHouse").value("George Allen & Unwin"))
            .andExpect(jsonPath("$.active").value(true));
    ArgumentCaptor<BookUpdateRequest> captor = ArgumentCaptor.forClass(BookUpdateRequest.class);
    verify(bookService).updateBook(eq(1), captor.capture());
    BookUpdateRequest capturedRequest = captor.getValue();
    assertEquals("Lord of the Rings", capturedRequest.title().get());
    assertEquals(JsonNullable.of("J.R.R. Tolkien"), capturedRequest.author());
    assertEquals(JsonNullable.of(1954), capturedRequest.releaseYear());
    assertEquals(JsonNullable.of("George Allen & Unwin"), capturedRequest.publishingHouse());
  }

  @Test
  void shouldReturn200WhenUpdatingAllAvailableFieldsExcludeTitleByNullValue() throws Exception {
    BookDTO bookDTO = new BookDTO(1, "Lord of the Rings", null,
            null, null, true);
    when(bookService.updateBook(eq(1), any(BookUpdateRequest.class)))
            .thenReturn(bookDTO);
    String request = """
            {
                "author":null,
                "releaseYear":null,
                "publishingHouse":null
            }
            """;
    mockMvc.perform(patch("/api/books/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Lord of the Rings"))
            .andExpect(jsonPath("$.author").value(is(nullValue())))
            .andExpect(jsonPath("$.releaseYear").value(is(nullValue())))
            .andExpect(jsonPath("$.publishingHouse").value(is(nullValue())))
            .andExpect(jsonPath("$.active").value(true));
    ArgumentCaptor<BookUpdateRequest> captor = ArgumentCaptor.forClass(BookUpdateRequest.class);
    verify(bookService).updateBook(eq(1), captor.capture());
    BookUpdateRequest capturedRequest = captor.getValue();
    assertEquals(JsonNullable.of(null), capturedRequest.author());
    assertEquals(JsonNullable.of(null), capturedRequest.releaseYear());
    assertEquals(JsonNullable.of(null), capturedRequest.publishingHouse());
  }

  @Test
  void shouldReturn200WhenChangingBookStatusToActive() throws Exception {
    BookDTO bookDTO = new BookDTO(1, "Lord of the Rings", "J.R.R. Tolkien",
            1954, "George Allen & Unwin", true);
    when(bookService.activateBook(1)).thenReturn(bookDTO);
    mockMvc.perform(patch("/api/books/1/activate")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.active").value(true));
    verify(bookService).activateBook(1);
  }

  @Test
  void shouldReturn409WhenChangingBookStatusToActive() throws Exception {
    when(bookService.activateBook(1))
            .thenThrow(new BookAlreadyActiveException(1));
    mockMvc.perform(patch("/api/books/1/activate")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Book with id 1 is already active"))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn404WhenChangingBookStatusToActiveAndBookDoesNotExist() throws Exception {
    when(bookService.activateBook(1))
            .thenThrow(new BookNotFoundException(1));
    mockMvc.perform(patch("/api/books/1/activate")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Book with id: 1 not found"))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn200WhenChangingBookStatusToInactive() throws Exception {
    BookDTO bookDTO = new BookDTO(1, "Lord of the Rings", "J.R.R. Tolkien",
            1954, "George Allen & Unwin", false);
    when(bookService.deactivateBook(1)).thenReturn(bookDTO);
    mockMvc.perform(patch("/api/books/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.active").value(false));
    verify(bookService).deactivateBook(1);
  }

  @Test
  void shouldReturn409WhenChangingBookStatusToInactive() throws Exception {
    when(bookService.deactivateBook(1))
            .thenThrow(new BookAlreadyInactiveException(1));
    mockMvc.perform(patch("/api/books/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Book with id 1 is already Inactive"))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn409WhenDeactivatingBookWithActiveBorrows() throws Exception {
    when(bookService.deactivateBook(1))
            .thenThrow(BookHasActiveBorrowsException.class);
    mockMvc.perform(patch("/api/books/1/deactivate"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.trace").doesNotExist());
    verify(bookService).deactivateBook(1);
  }

  @Test
  void shouldReturn404WhenChangingBookStatusToInactiveAndBookDoesNotExist() throws Exception {
    when(bookService.deactivateBook(1))
            .thenThrow(new BookNotFoundException(1));
    mockMvc.perform(patch("/api/books/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Book with id: 1 not found"))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }
}