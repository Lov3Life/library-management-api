package pl.bartek.librarymanagementapi.readers;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReaderRepository extends JpaRepository<Reader, Integer> {

  List<Reader> findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(String firstName, String lastName);

  List<Reader> findByFirstNameContainingIgnoreCase(String firstName);

  List<Reader> findByLastNameContainingIgnoreCase(String lastName);
}
