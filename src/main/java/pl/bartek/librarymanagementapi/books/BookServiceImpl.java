package pl.bartek.librarymanagementapi.books;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartek.librarymanagementapi.books.exceptions.BookAlreadyActiveException;
import pl.bartek.librarymanagementapi.books.exceptions.BookAlreadyInactiveException;
import pl.bartek.librarymanagementapi.books.exceptions.BookCreateInvalidParameterException;
import pl.bartek.librarymanagementapi.books.exceptions.BookNotFoundException;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;

  public BookServiceImpl(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @Override
  public List<BookDTO> getBooks(String title, Boolean active){
    List<Book> books;
    boolean hasTitle = title != null && !title.isEmpty();
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
  public BookDTO saveBook(BookCreateRequest request){
    Book book = BookCreateRequest.toBook(request);
    if(normalize(book.getTitle()) == null){
      throw new BookCreateInvalidParameterException("Book title is required");
    }
    book.setTitle(normalize(request.title()));
    book.setAuthor(normalize(request.author()));
    book.setPublishingHouse(normalize(request.publishingHouse()));
    return BookDTO.fromBook(bookRepository.save(book));
  }

  @Override
  @Transactional
  public BookDTO updateBook(Integer id, BookUpdateRequest request){
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    if(normalize(request.title()) != null){
      book.setTitle(request.title().trim());
    }
    if(request.author() != null){
      book.setAuthor(normalize(request.author()));
    }
    if(request.releaseYear() != null){
      book.setReleaseYear(request.releaseYear());
    }
    if(request.publishingHouse() != null){
      book.setPublishingHouse(normalize(request.publishingHouse()));
    }
    return BookDTO.fromBook(bookRepository.save(book));
  }

  @Override
  @Transactional
  public BookDTO deactivateBook(Integer id) {
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(id));
    if(!book.isActive()){
      throw new BookAlreadyInactiveException(id);
    }
    book.setActive(false);
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
    return BookDTO.fromBook(bookRepository.save(book));
  }

  private String normalize(String input){
    if(input == null || input.isBlank()){
      return null;
    }
    return input.trim();
  }
}
