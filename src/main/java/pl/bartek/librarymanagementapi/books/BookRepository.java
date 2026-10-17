package pl.bartek.librarymanagementapi.books;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {

  List<Book> findByTitleContainingIgnoreCase(String title);

  List<Book> findByTitleContainingIgnoreCaseAndActive(String title, boolean active);

  List<Book> findByActive(boolean active);
}
