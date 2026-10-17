package pl.bartek.librarymanagementapi.books;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.books.exceptions.BookNotFoundException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
public class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private BookService bookService;

  @Test
  void shouldReturn404WhenBookDoesNotExist() throws Exception{
    when(bookService.getBook(999))
            .thenThrow(new BookNotFoundException(999));

    mockMvc.perform(get("/api/books/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Book with id: 999 not found"))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn200WhenBookExists() throws Exception{
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
}
