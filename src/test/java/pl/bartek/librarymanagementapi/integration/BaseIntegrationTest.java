package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.books.BookRepository;
import pl.bartek.librarymanagementapi.borrows.BorrowRepository;
import pl.bartek.librarymanagementapi.integration.config.MySqlTestConfiguration;
import pl.bartek.librarymanagementapi.logs.LogRepository;
import pl.bartek.librarymanagementapi.readers.ReaderRepository;

import java.time.Clock;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MySqlTestConfiguration.class)
public abstract class BaseIntegrationTest {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected LogRepository logRepository;

  @Autowired
  protected ReaderRepository readerRepository;

  @Autowired
  protected BorrowRepository borrowRepository;

  @Autowired
  protected BookRepository bookRepository;

  @Autowired
  protected Clock clock;

  @BeforeEach
  protected void cleanDatabase() {
    borrowRepository.deleteAll();
    logRepository.deleteAll();
    readerRepository.deleteAll();
    bookRepository.deleteAll();
  }
}
