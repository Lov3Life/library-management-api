package pl.bartek.librarymanagementapi.books;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartek.librarymanagementapi.books.exceptions.*;
import pl.bartek.librarymanagementapi.borrows.BorrowRepository;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.logs.LogService;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;
  private final BorrowRepository borrowRepository;
  private final LogService logService;

  public BookServiceImpl(BookRepository bookRepository, BorrowRepository borrowRepository, LogService logService) {
    this.bookRepository = bookRepository;
    this.borrowRepository = borrowRepository;
    this.logService = logService;
  }

  @Override
  public List<BookDTO> getBooks(String title, Boolean active){
    List<Book> books;
    if(title != null){
      title = normalize(title);
    }
    boolean hasTitle = title != null && !title.isBlank();
    if(hasTitle && active != null){
      books = bookRepository.findByTitleContainingIgnoreCaseAndActive(title, active);
    } else if(hasTitle) {
      books = bookRepository.findByTitleContainingIgnoreCase(title);
    } else if(active != null) {
      books = bookRepository.findByActive(active);
    }else {
      books = bookRepository.findAll();
    }
    return books.stream()
            .map(BookDTO::fromBook)
            .toList();
  }

  @Override
  public BookDTO getBook(Integer id){
    return bookRepository.findById(id)
            .map(BookDTO::fromBook)
            .orElseThrow(() -> new BookNotFoundException(id));
  }

  @Override
  @Transactional
  public BookDTO saveBook(BookCreateRequest request){
    Book book = BookCreateRequest.toBook(request);
    if(normalize(book.getTitle()) == null){
      throw new BookCreateInvalidParameterException("Book title is required");
    }
    book.setTitle(normalize(request.title()));
    book.setAuthor(normalize(request.author()));
    book.setPublishingHouse(normalize(request.publishingHouse()));
    Book savedBook = bookRepository.save(book);
    logService.logEvent(LogEventType.BOOK_CREATED, null, savedBook);
    return BookDTO.fromBook(savedBook);
  }

  @Override
  @Transactional
  public BookDTO updateBook(Integer id, BookUpdateRequest request){
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    BookDTO prevState = BookDTO.fromBook(book);
    if(request.title().isPresent()){
      String title = request.title().get();
      if(title == null || title.isBlank()){
        throw new BookUpdateInvalidParameterException(
                "Book title cannot be blank or Null. If change is no needed, don't fill param");
      }
      book.setTitle(title.trim());
    }
    if(request.author().isPresent()){
      book.setAuthor(normalize(request.author().get()));
    }
    if(request.releaseYear().isPresent()){
      book.setReleaseYear(request.releaseYear().get());
    }
    if(request.publishingHouse().isPresent()){
      book.setPublishingHouse(normalize(request.publishingHouse().get()));
    }
    BookDTO current = BookDTO.fromBook(book);
    if(prevState.equals(current)){
      return current;
    }else {
      logService.logEvent(LogEventType.BOOK_UPDATED, null, book, null, prevState);
      return BookDTO.fromBook(bookRepository.save(book));
    }
  }

  @Override
  @Transactional
  public BookDTO deactivateBook(Integer id) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    if(!book.isActive()){
      throw new BookAlreadyInactiveException(id);
    }
    if(borrowRepository.existsByBookIdAndReturnDateIsNull(id)){
      throw new BookHasActiveBorrowsException(id);
    }
    book.setActive(false);
    logService.logEvent(LogEventType.BOOK_DEACTIVATED,null, book);
    return BookDTO.fromBook(bookRepository.save(book));
  }

  @Override
  @Transactional
  public BookDTO activateBook(Integer id) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    if(book.isActive()){
        throw new BookAlreadyActiveException(id);
    }
    book.setActive(true);
    logService.logEvent(LogEventType.BOOK_ACTIVATED,null, book);
    return BookDTO.fromBook(bookRepository.save(book));
  }

  private String normalize(String input){
    if(input == null || input.isBlank()){
      return null;
    }
    return input.trim();
  }
}
