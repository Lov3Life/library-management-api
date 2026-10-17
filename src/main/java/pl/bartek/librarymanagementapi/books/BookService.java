package pl.bartek.librarymanagementapi.books;

import java.util.List;

public interface BookService {

  List<BookDTO> getBooks(String title, Boolean active);

  BookDTO getBook(Integer id);

  BookDTO saveBook(BookCreateRequest request);

  BookDTO updateBook(Integer id, BookUpdateRequest request);

  BookDTO deactivateBook(Integer id);

  BookDTO activateBook(Integer id);
}
