package pl.bartek.librarymanagementapi.books;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.books.exceptions.BookCreateInvalidParameterException;
import pl.bartek.librarymanagementapi.books.exceptions.BookNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

  @Test
  void shouldThrowBookCreateInvalidParameterExceptionWhenTitleIsNullInBookSaveRequest() {
    BookCreateRequest request = new BookCreateRequest(null, "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    assertThrows(BookCreateInvalidParameterException.class, () -> bookService.saveBook(request));
  }

  @Test
  void shouldThrowBookCreateInvalidParameterExceptionWhenTitleIsEmptyInBookSaveRequest() {
    BookCreateRequest request = new BookCreateRequest("", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    assertThrows(BookCreateInvalidParameterException.class, () -> bookService.saveBook(request));
  }

  @Test
  void shouldThrowBookCreateInvalidParameterExceptionWhenTitleIsBlankInBookSaveRequest() {
    BookCreateRequest request = new BookCreateRequest("   ", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    assertThrows(BookCreateInvalidParameterException.class, () -> bookService.saveBook(request));
  }

  @Test
  void shouldSaveBookWithNormalizationInBook(){
    BookCreateRequest request = new BookCreateRequest("   The Hobbit   ", "     J.R.R. Tolkien ",
            1937, "      George Allen & Unwin ");
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO bookDTO = bookService.saveBook(request);
    BookDTO expected = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);
    assertEquals(expected.title(), bookDTO.title());
    assertEquals(expected.author(), bookDTO.author());
    assertEquals(expected.releaseYear(), bookDTO.releaseYear());
    assertEquals(expected.publishingHouse(), bookDTO.publishingHouse());
  }

  @Test
  void shouldUpdateBookWithAllProperFields(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequest = new BookUpdateRequest(
            "The Lord of the Rings", "J.R.R. Tolkien", 1954, "Allen & Unwin");
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO updatedBookDTO = bookService.updateBook(1, bookUpdateRequest);
    assertEquals(bookUpdateRequest.title(), updatedBookDTO.title());
    assertEquals(bookUpdateRequest.author(), updatedBookDTO.author());
    assertEquals(bookUpdateRequest.releaseYear(), updatedBookDTO.releaseYear());
    assertEquals(bookUpdateRequest.publishingHouse(), updatedBookDTO.publishingHouse());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldUpdateBookWithOnlyTitle(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1954, "Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequest = new BookUpdateRequest("The Lord of the Rings", null, null, null);
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO updatedBookDTO = bookService.updateBook(1, bookUpdateRequest);
    assertEquals(bookUpdateRequest.title(), updatedBookDTO.title());
    assertNotEquals(bookUpdateRequest.author(), updatedBookDTO.author());
    assertNotEquals(bookUpdateRequest.releaseYear(), updatedBookDTO.releaseYear());
    assertNotEquals(bookUpdateRequest.publishingHouse(), updatedBookDTO.publishingHouse());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldNotUpdateWithEmptyParam(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequest = new BookUpdateRequest(null, null, null, null);
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO updatedBookDTO = bookService.updateBook(1, bookUpdateRequest);
    assertNotEquals(bookUpdateRequest.title(), updatedBookDTO.title());
    assertNotEquals(bookUpdateRequest.author(), updatedBookDTO.author());
    assertNotEquals(bookUpdateRequest.releaseYear(), updatedBookDTO.releaseYear());
    assertNotEquals(bookUpdateRequest.publishingHouse(), updatedBookDTO.publishingHouse());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldUpdateWithNormalizeParams(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequestWithoutNormalization = new BookUpdateRequest(
            "   The Lord of the Rings   ",
            "                      J.R.R. Tolkien         ",
            1937,
            " George Allen & Unwin      ");
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO updatedBookDTO = bookService.updateBook(1, bookUpdateRequestWithoutNormalization);
    BookDTO testDTOWithNormalization = new BookDTO(
            1,
            "The Lord of the Rings",
            "J.R.R. Tolkien",
            1937,
            "George Allen & Unwin",
            true);
    assertEquals(testDTOWithNormalization, updatedBookDTO);
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }
}
