package pl.bartek.librarymanagementapi.logs;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<Log, Integer>{

  Page<Log> findByReaderId(Integer readerId, Pageable pageable);

  Page<Log> findByBookId(Integer bookId, Pageable pageable);
}
