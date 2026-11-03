package pl.bartek.librarymanagementapi.borrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookRepository;
import pl.bartek.librarymanagementapi.books.exceptions.BookNotFoundException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowAlreadyReturnedException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowConflictException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowNotFoundException;
import pl.bartek.librarymanagementapi.logs.LogService;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderRepository;
import pl.bartek.librarymanagementapi.readers.exceptions.ReaderNotFoundException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BorrowServiceTest {

  @Mock
  private BorrowRepository borrowRepository;

  @Mock
  private ReaderRepository readerRepository;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private LogService logService;

  private BorrowServiceImpl borrowService;

  private static final Clock FIXED_CLOCK = Clock.fixed(
          Instant.parse("2022-01-01T00:00:00Z"),
          ZoneId.of("Europe/Warsaw"));

  private static final LocalDate TODAY = LocalDate.now(FIXED_CLOCK);

  @BeforeEach
  void setUp() {
    borrowService = new BorrowServiceImpl(borrowRepository, bookRepository, readerRepository, logService, FIXED_CLOCK);
  }

  @Test
  void shouldThrowBorrowNotFoundExceptionWhenBorrowDoesNotExist() {
    when(borrowRepository.findById(anyInt()))
            .thenReturn(Optional.empty());
    assertThrows(BorrowNotFoundException.class, () -> borrowService.getBorrow(1));
    verify(borrowRepository).findById(anyInt());
  }

  @Test
  void shouldReturnBorrowWhenBorrowExists() {
    Reader reader = new Reader();
    ReflectionTestUtils.setField(reader, "id", 1);
    Book book = new Book();
    ReflectionTestUtils.setField(book, "id", 1);
    Borrow borrow = new Borrow(book, reader, LocalDate.of(2023, 1, 1));
    ReflectionTestUtils.setField(borrow, "id", 1);
    when(borrowRepository.findById(anyInt()))
            .thenReturn(Optional.of(borrow));
    BorrowDTO borrowDTO = borrowService.getBorrow(1);
    BorrowDTO expected = new BorrowDTO(1, book.getId(), reader.getId(),
            LocalDate.of(2023, 1, 1), null);
    assertEquals(expected, borrowDTO);
    verify(borrowRepository).findById(anyInt());
  }

  @Test
  void shouldReturnAllBorrows(){
    Reader reader = new Reader();
    ReflectionTestUtils.setField(reader, "id", 1);
    Book book1 = new Book();
    ReflectionTestUtils.setField(book1, "id", 1);
    Book book2 = new Book();
    ReflectionTestUtils.setField(book2, "id", 2);
    Borrow borrow1 = new Borrow(book1, reader, LocalDate.of(2023, 1, 1));
    ReflectionTestUtils.setField(borrow1, "id", 1);
    Borrow borrow2 = new Borrow(book2, reader, LocalDate.of(2023, 1, 1));
    ReflectionTestUtils.setField(borrow2, "id", 2);
    List<Borrow> borrows = List.of(borrow1, borrow2);
    when(borrowRepository.findByFilter(null, null, null))
            .thenReturn(borrows);
    List<BorrowDTO> result = borrowService.getBorrows(null, null, null);
    List<BorrowDTO> expected = List.of(
            new BorrowDTO(1, book1.getId(), reader.getId(),
                    LocalDate.of(2023, 1, 1), null),
            new BorrowDTO(2, book2.getId(), reader.getId(),
                    LocalDate.of(2023, 1, 1), null)
    );
    assertEquals(expected, result);
    verify(borrowRepository).findByFilter(null, null, null);
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
  void shouldPassFiltersToRepository(
          Integer readerId,
          Integer bookId,
          Boolean returned) {
    when(borrowRepository.findByFilter(readerId, bookId, returned))
            .thenReturn(List.of());
    List<BorrowDTO> result = borrowService.getBorrows(readerId, bookId, returned);
    assertTrue(result.isEmpty());
    verify(borrowRepository).findByFilter(readerId, bookId, returned);
  }

  @Test
  void shouldReturnBookNotFoundExceptionWhenBorrowNotFoundWhenBorrowing(){
    when(bookRepository.findById(anyInt()))
            .thenReturn(Optional.empty());
    BorrowCreateRequest request = new BorrowCreateRequest(1, 1);
    assertThrows(BookNotFoundException.class,
            () -> borrowService.createBorrow(request));
    verify(borrowRepository, never()).save(any(Borrow.class));
  }

  @Test
  void shouldReturnReaderNotFoundExceptionWhenBorrowNotFoundWhenBorrowing(){
    when(bookRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Book()));
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.empty());
    BorrowCreateRequest request = new BorrowCreateRequest(1, 1);
    assertThrows(ReaderNotFoundException.class,
            () -> borrowService.createBorrow(request));
    verify(borrowRepository, never()).save(any(Borrow.class));
  }

  @Test
  void shouldReturnBorrowConflictExceptionWhenBookIsAlreadyBorrowed(){
    when(bookRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Book()));
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    when(borrowRepository.existsByBookIdAndReturnDateIsNull(anyInt()))
            .thenReturn(true);
    BorrowCreateRequest request = new BorrowCreateRequest(1, 1);
    assertThrows(BorrowConflictException.class,
            () -> borrowService.createBorrow(request));
    verify(borrowRepository, never()).save(any(Borrow.class));
  }

  @Test
  void shouldReturnBorrowConflictExceptionWhenReaderIsNotActive(){
    when(bookRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Book()));
    Reader reader = new Reader();
    reader.setActive(false);
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(borrowRepository.existsByBookIdAndReturnDateIsNull(anyInt()))
            .thenReturn(false);
    BorrowCreateRequest request = new BorrowCreateRequest(1, 1);
    assertThrows(BorrowConflictException.class,
            () -> borrowService.createBorrow(request));
    verify(borrowRepository, never()).save(any(Borrow.class));
  }

  @Test
  void shouldReturnBorrowConflictExceptionWhenBookIsNotActive(){
    Book book = new Book();
    book.setActive(false);
    when(bookRepository.findById(anyInt()))
            .thenReturn(Optional.of(book));
    Reader reader = new Reader();
    reader.setActive(true);
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(borrowRepository.existsByBookIdAndReturnDateIsNull(anyInt()))
            .thenReturn(false);
    BorrowCreateRequest request = new BorrowCreateRequest(1, 1);
    assertThrows(BorrowConflictException.class,
            () -> borrowService.createBorrow(request));
    verify(borrowRepository, never()).save(any(Borrow.class));
  }

  @Test
  void shouldCreateBorrow(){
    Book book = new Book();
    book.setActive(true);
    ReflectionTestUtils.setField(book, "id", 1);
    when(bookRepository.findById(anyInt()))
            .thenReturn(Optional.of(book));
    Reader reader = new Reader();
    reader.setActive(true);
    ReflectionTestUtils.setField(reader, "id", 1);
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(borrowRepository.existsByBookIdAndReturnDateIsNull(anyInt()))
            .thenReturn(false);
    when(borrowRepository.save(any(Borrow.class)))
            .thenAnswer(invocation -> {
              Borrow saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "id", 10);
              return saved;
            });
    BorrowDTO result = borrowService.createBorrow(new BorrowCreateRequest(1, 1));
    BorrowDTO expected = new BorrowDTO(10, 1, 1, TODAY, null);
    assertEquals(expected, result);
    ArgumentCaptor<Borrow> captor = ArgumentCaptor.forClass(Borrow.class);
    verify(borrowRepository).save(captor.capture());
    Borrow savedBorrow = captor.getValue();
    assertSame(book, savedBorrow.getBook());
    assertSame(reader, savedBorrow.getReader());
    assertEquals(TODAY, savedBorrow.getBorrowDate());
    assertNull(savedBorrow.getReturnDate());
  }

  @Test
  void shouldThrowBorrowNotFoundExceptionWhenBorrowDoesNotExistWhenReturning(){
    when(borrowRepository.findById(anyInt()))
            .thenReturn(Optional.empty());
    assertThrows(BorrowNotFoundException.class,
            () -> borrowService.returnBook(1));
    verify(borrowRepository, never()).save(any(Borrow.class));
  }

  @Test
  void shouldThrowBorrowAlreadyReturnedExceptionWhenBorrowIsAlreadyReturned(){
    Reader reader = new Reader();
    ReflectionTestUtils.setField(reader, "id", 1);
    Book book = new Book();
    ReflectionTestUtils.setField(book, "id", 1);
    Borrow borrow = new Borrow(book, reader, TODAY);
    ReflectionTestUtils.setField(borrow, "id", 1);
    borrow.returnBook(TODAY.plusDays(1));
    when(borrowRepository.findById(anyInt()))
            .thenReturn(Optional.of(borrow));
    assertThrows(BorrowAlreadyReturnedException.class,
            () -> borrowService.returnBook(1));
    verify(borrowRepository, never()).save(any(Borrow.class));
  }

  @Test
  void shouldReturnBookSuccessfully(){
    Reader reader = new Reader();
    ReflectionTestUtils.setField(reader, "id", 1);
    Book book = new Book();
    ReflectionTestUtils.setField(book, "id", 1);
    Borrow borrow = new Borrow(book, reader, TODAY.minusDays(1));
    ReflectionTestUtils.setField(borrow, "id", 1);
    when(borrowRepository.findById(anyInt()))
            .thenReturn(Optional.of(borrow));
    when(borrowRepository.save(any(Borrow.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    BorrowDTO result = borrowService.returnBook(1);
    BorrowDTO expected = new BorrowDTO(1, 1, 1,
            TODAY.minusDays(1), TODAY);
    assertNotNull(result);
    assertEquals(expected, result);
    verify(borrowRepository).save(any(Borrow.class));
  }
}
