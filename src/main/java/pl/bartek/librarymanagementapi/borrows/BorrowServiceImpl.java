package pl.bartek.librarymanagementapi.borrows;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookRepository;
import pl.bartek.librarymanagementapi.books.exceptions.BookNotFoundException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowAlreadyReturnedException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowConflictException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowNotFoundException;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.logs.LogService;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderRepository;
import pl.bartek.librarymanagementapi.readers.exceptions.ReaderNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowServiceImpl implements BorrowService {

  private final BorrowRepository borrowRepository;
  private final BookRepository bookRepository;
  private final ReaderRepository readerRepository;
  private final LogService logService;
  private final Clock clock;

  public BorrowServiceImpl(BorrowRepository borrowRepository, BookRepository bookRepository,
                           ReaderRepository readerRepository, LogService logService, Clock clock) {
    this.borrowRepository = borrowRepository;
    this.bookRepository = bookRepository;
    this.readerRepository = readerRepository;
    this.logService = logService;
    this.clock = clock;
  }

  @Override
  @Transactional(readOnly = true)
  public List<BorrowDTO> getBorrows(Integer readerId, Integer bookId, Boolean returned) {
    return borrowRepository.findByFilter(readerId, bookId, returned)
            .stream()
            .map(BorrowDTO::fromBorrow)
            .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public BorrowDTO getBorrow(Integer id) {
    return borrowRepository.findById(id)
            .map(BorrowDTO::fromBorrow)
            .orElseThrow(() -> new BorrowNotFoundException(id));
  }

  @Override
  @Transactional
  public BorrowDTO createBorrow(BorrowCreateRequest request) {
    Integer bookId = request.bookId();
    Integer readerId = request.readerId();
    Book book = bookRepository.findById(bookId).orElseThrow(
            () -> new BookNotFoundException(bookId));
    Reader reader = readerRepository.findById(readerId).orElseThrow(
            () -> new ReaderNotFoundException(readerId));
    if(borrowRepository.existsByBookIdAndReturnDateIsNull(bookId))
      throw new BorrowConflictException("Book with id: " + bookId + " is already borrowed.");
    if(!reader.isActive())
      throw new BorrowConflictException("Reader with id: " + readerId + " is not active.");
    if(!book.isActive())
      throw new BorrowConflictException("Book with id: " + bookId + " is not active.");
    Borrow borrow = new Borrow(book, reader, LocalDate.now(clock));
    logService.logEvent(LogEventType.BOOK_BORROWED, reader, book);
    return BorrowDTO.fromBorrow(borrowRepository.save(borrow));
  }

  @Override
  @Transactional
  public BorrowDTO returnBook(Integer id) {
    Borrow borrow = borrowRepository.findById(id).orElseThrow(
            () -> new BorrowNotFoundException(id));
    if(borrow.isReturned())
      throw new BorrowAlreadyReturnedException("Borrow with id: " + id + " has already been returned.");
    borrow.returnBook(LocalDate.now(clock));
    logService.logEvent(LogEventType.BOOK_RETURNED, borrow.getReader(), borrow.getBook());
    return BorrowDTO.fromBorrow(borrowRepository.save(borrow));
  }
}
