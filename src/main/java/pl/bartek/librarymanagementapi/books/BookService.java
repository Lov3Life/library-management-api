package pl.bartek.librarymanagementapi.books;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

  private final BookRepository bookRepository;

  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public List<BookDTO> getBooks(String title){
    List<Book> books = title == null || title.isEmpty()
            ? bookRepository.findAll()
            : bookRepository.findByTitleContainingIgnoreCase(title);
    return books.stream()
            .map(BookDTO::fromBook)
            .toList();
  }

  public BookDTO getBook(Integer id){
    return bookRepository.findById(id)
            .map(BookDTO::fromBook)
            .orElseThrow(() -> new IllegalArgumentException("Book with id " + id + " not found"));
  }

  public BookDTO saveBook(BookCreateRequest request){
    Book book = BookCreateRequest.toBook(request);
    return BookDTO.fromBook(bookRepository.save(book));
  }

  @Transactional
  public BookDTO updateBook(Integer id, BookUpdateRequest request){
    Book book = bookRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Book with id " + id + " not found"));
    if(request.title() != null){
      book.setTitle(request.title().trim());
    }
    if(request.author() != null){
      book.setAuthor(normalize(request.author()));
    }
    if(request.releaseYear() != null){
      book.setReleaseYear(request.releaseYear());
    }
    if(request.publishingHouse() != null){
      book.setPublishingHouse(normalize(request.publishingHouse().trim()));
    }
    return BookDTO.fromBook(bookRepository.save(book));
  }

  private String normalize(String input){
    if(input == null || input.isEmpty()){
      return null;
    }
    return input.trim();
  }
}
