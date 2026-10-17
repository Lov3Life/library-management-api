package pl.bartek.librarymanagementapi.books;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.books.exceptions.BookNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookServiceImpl bookService;

  @Test
  void shouldThrowBookNotFoundExceptionWhenBookDoesNotExist() {
    when(bookRepository.findById(999))
            .thenReturn(Optional.empty());
    assertThrows(BookNotFoundException.class, () -> bookService.getBook(999));
    verify(bookRepository).findById(999);
  }

  @Test
  void shouldReturnBookWhenBookExists() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));

    BookDTO bookDTO = bookService.getBook(1);

    BookDTO expected = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);

    assertEquals(expected, bookDTO);
    verify(bookRepository).findById(1);
  }
}
