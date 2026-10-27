package pl.bartek.librarymanagementapi.books;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.books.exceptions.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
  void shouldReturnAllBooks() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    Book book2 = new Book("The Lord of the Rings", "J.R.R. Tolkien",
            1954, "George Allen & Unwin");
    when(bookRepository.findAll())
            .thenReturn(List.of(book, book2));
    List<BookDTO> books = bookService.getBooks(null, null);
    assertEquals(2, books.size());
    assertEquals("The Hobbit", books.get(0).title());
    assertEquals("The Lord of the Rings", books.get(1).title());
    verify(bookRepository).findAll();
  }

  @Test
  void shouldTrimTitleBeforeSearching(){
    when(bookRepository.findByTitleContainingIgnoreCase("Hobbit"))
            .thenReturn(List.of());
    bookService.getBooks("   Hobbit   ", null);
    verify(bookRepository).findByTitleContainingIgnoreCase("Hobbit");
  }

  @Test
  void shouldReturnBookWhenFilterByTitle() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    when(bookRepository.findByTitleContainingIgnoreCase("The Hobbit"))
            .thenReturn(List.of(book));
    List<BookDTO> books = bookService.getBooks("The Hobbit", null);
    assertEquals(1, books.size());
    assertEquals("The Hobbit", books.getFirst().title());
    verify(bookRepository).findByTitleContainingIgnoreCase("The Hobbit");
  }

  @Test
  void shouldReturnBookWhenFilterByActiveTrue() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    when(bookRepository.findByActive(true))
            .thenReturn(List.of(book));
    List<BookDTO> books = bookService.getBooks(null, true);
    assertEquals(1, books.size());
    assertTrue(books.getFirst().active());
    verify(bookRepository).findByActive(true);
  }

  @Test
  void shouldReturnBookWhenFilterByActiveFalse() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    book.setActive(false);
    when(bookRepository.findByActive(false))
            .thenReturn(List.of(book));
    List<BookDTO> books = bookService.getBooks(null, false);
    assertEquals(1, books.size());
    assertFalse(books.getFirst().active());
    verify(bookRepository).findByActive(false);
  }

  @Test
  void shouldReturnBookWhenFilterByTitleAndActiveFalse() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    book.setActive(false);
    when(bookRepository.findByTitleContainingIgnoreCaseAndActive("The Hobbit", false))
            .thenReturn(List.of(book));
    List<BookDTO> books = bookService.getBooks("The Hobbit", false);
    assertEquals(1, books.size());
    assertFalse(books.getFirst().active());
    verify(bookRepository).findByTitleContainingIgnoreCaseAndActive("The Hobbit", false);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "    ", "\t", "\n"})
  void shouldThrowBookCreateInvalidParameterExceptionWhenTitleIsInvalidInBookSaveRequest(String title){
    BookCreateRequest request = new BookCreateRequest(title, "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    assertThrows(BookCreateInvalidParameterException.class, () -> bookService.saveBook(request));
  }

  @Test
  void shouldSaveBookWithNormalizationInBook() {
    BookCreateRequest request = new BookCreateRequest("   The Hobbit   ", "     J.R.R. Tolkien ",
            1937, "      George Allen & Unwin ");
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO bookDTO = bookService.saveBook(request);
    assertEquals("The Hobbit", bookDTO.title());
    assertEquals("J.R.R. Tolkien", bookDTO.author());
    assertEquals(1937, bookDTO.releaseYear());
    assertEquals("George Allen & Unwin", bookDTO.publishingHouse());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {" ", "     ", "\t", "\n"})
  void shouldThrowUpdateInvalidParameterExceptionWhenUpdateTitleIsInvalid(String title){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    BookUpdateRequest request = new BookUpdateRequest(JsonNullable.of(title), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    assertThrows(BookUpdateInvalidParameterException.class,
            () -> bookService.updateBook(1, request));
    verify(bookRepository).findById(1);
    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  void shouldUpdateBookWithAllProperFields() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequest = new BookUpdateRequest(
            JsonNullable.of("The Lord of the Rings"), JsonNullable.of("J.R.R. Tolkien"),
            JsonNullable.of(1954), JsonNullable.of("Allen & Unwin"));
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO updatedBookDTO = bookService.updateBook(1, bookUpdateRequest);
    assertEquals(bookUpdateRequest.title().get(), updatedBookDTO.title());
    assertEquals(bookUpdateRequest.author().get(), updatedBookDTO.author());
    assertEquals(bookUpdateRequest.releaseYear().get(), updatedBookDTO.releaseYear());
    assertEquals(bookUpdateRequest.publishingHouse().get(), updatedBookDTO.publishingHouse());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldUpdateBookWithOnlyTitle() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1954, "Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequest = new BookUpdateRequest(
            JsonNullable.of("The Lord of the Rings"), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO updatedBookDTO = bookService.updateBook(1, bookUpdateRequest);
    assertEquals(bookUpdateRequest.title().get(), updatedBookDTO.title());
    assertEquals("J.R.R. Tolkien", updatedBookDTO.author());
    assertEquals(1954, updatedBookDTO.releaseYear());
    assertEquals("Allen & Unwin", updatedBookDTO.publishingHouse());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldLeaveBookUnchangedWhenAllFieldsUndefined() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequest = new BookUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO expected = new BookDTO(1, "The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin", true);
    BookDTO actual = bookService.updateBook(1, bookUpdateRequest);
    assertEquals(expected, actual);
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldUpdateWithNormalizeParams() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookUpdateRequestWithoutNormalization = new BookUpdateRequest(
            JsonNullable.of("   The Lord of the Rings   "),
            JsonNullable.of("                      J.R.R. Tolkien         "),
            JsonNullable.of(1937),
            JsonNullable.of(" George Allen & Unwin      "));
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

  @Test
  void shouldClearOptionalFieldsWhenExplicitNullsProvided() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookUpdateRequest bookOfNull = new BookUpdateRequest(JsonNullable.undefined(), JsonNullable.of(null),
            JsonNullable.of(null), JsonNullable.of(null));
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO updatedBookDTO = bookService.updateBook(1, bookOfNull);
    assertEquals("The Hobbit", updatedBookDTO.title());
    assertNull(updatedBookDTO.author());
    assertNull(updatedBookDTO.releaseYear());
    assertNull(updatedBookDTO.publishingHouse());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldThrowExceptionWhenBookIsAlreadyActive() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    book.setActive(true);
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    assertThrows(BookAlreadyActiveException.class, () -> bookService.activateBook(1));
    verify(bookRepository).findById(1);
  }

  @Test
  void shouldChangeBookActiveStatusToActive() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    book.setActive(false);
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO result = bookService.activateBook(1);
    assertTrue(result.active());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }

  @Test
  void shouldThrowExceptionWhenBookIsAlreadyInactive() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    book.setActive(false);
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    assertThrows(BookAlreadyInactiveException.class, () -> bookService.deactivateBook(1));
    verify(bookRepository).findById(1);
  }

  @Test
  void shouldChangeBookActiveStatusToInactive() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    book.setActive(true);
    when(bookRepository.findById(1))
            .thenReturn(Optional.of(book));
    when(bookRepository.save(any(Book.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BookDTO result = bookService.deactivateBook(1);
    assertFalse(result.active());
    verify(bookRepository).findById(1);
    verify(bookRepository).save(book);
  }
}