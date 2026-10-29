package pl.bartek.librarymanagementapi.logs;

import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Test;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookDTO;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderDTO;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.bartek.librarymanagementapi.logs.LogEventType.*;

public class LogDescriptionBuilderTest {

  private final LogDescriptionBuilder logDescriptionBuilder = new LogDescriptionBuilder();

  @Test
  void shouldBuildBookActivatedMessage() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    String result = logDescriptionBuilder.buildMessage(BOOK_ACTIVATED, null, book, null, null);
    assertEquals("Book The Hobbit activated.", result);
  }

  @Test
  void shouldBuildBookDeactivatedMessage() {
    Book book = new Book("The Hobbit", "J.R.R. Tolkien",
            1937, "George Allen & Unwin");
    String result = logDescriptionBuilder.buildMessage(BOOK_DEACTIVATED, null, book, null, null);
    assertEquals("Book The Hobbit deactivated.", result);
  }

  @Test
  void shouldBuildReaderActivatedMessage() {
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    String result = logDescriptionBuilder.buildMessage(READER_ACTIVATED, reader, null, null, null);
    assertEquals("Reader John Smith activated.", result);
  }

  @Test
  void shouldBuildReaderDeactivatedMessage() {
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    String result = logDescriptionBuilder.buildMessage(READER_DEACTIVATED, reader, null, null, null);
    assertEquals("Reader John Smith deactivated.", result);
  }

  @Test
  void shouldBuildBookUpdateMessage(){
    Book book = new Book("1984", "George Orwell", 1949, "Secker & Warburg");
    ReflectionTestUtils.setField(book, "id", 1);
    BookDTO previous = new BookDTO(1, "The Hobbit", null,
            1937, "George Allen & Unwin", true);
    String result = logDescriptionBuilder.buildMessage(BOOK_UPDATED, null, book, null, previous);
    assertEquals("Book with id 1 updated. Title: The Hobbit -> 1984. Author: null -> George Orwell. " +
                    "ReleaseYear: 1937 -> 1949. PublishingHouse: George Allen & Unwin -> Secker & Warburg.", result);
  }

  @Test
  void shouldBuildBookUpdateMessageWhenAuthorChangesFromNull(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    BookDTO previous = new BookDTO(1, "The Hobbit", null,
            1937, "George Allen & Unwin", true);
    String result = logDescriptionBuilder.buildMessage(BOOK_UPDATED, null, book, null, previous);
    assertEquals("Book with id 1 updated. Author: null -> J.R.R. Tolkien.", result);
  }

  @Test
  void shouldBuildReaderUpdateMessage(){
    Reader reader = new Reader("John", "James", "Smith", Date.valueOf("1998-05-21").toLocalDate());
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderDTO previous = new ReaderDTO(1, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    String result = logDescriptionBuilder.buildMessage(READER_UPDATED, reader, null, previous, null);
    assertEquals("Reader with id 1 updated. FirstName: Edward -> John. " +
            "LastName: Kenway -> Smith. BirthDate: 1693-03-10 -> 1998-05-21.", result);
  }

  @Test
  void shouldBuildReaderUpdateMessageWhenMiddleNameChangesFromNull(){
    Reader reader = new Reader("John", "Max", "Smith", Date.valueOf("1998-05-21").toLocalDate());
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderDTO previous = new ReaderDTO(1, "John", null,
            "Smith", Date.valueOf("1998-05-21").toLocalDate(), true);
    String result = logDescriptionBuilder.buildMessage(READER_UPDATED, reader, null, previous, null);
    assertEquals("Reader with id 1 updated. MiddleName: null -> Max.", result);
  }

  @Test
  void shouldBuildReaderCreateMessage(){
    Reader reader = new Reader("John", "James", "Smith", Date.valueOf("1998-05-21").toLocalDate());
    ReflectionTestUtils.setField(reader, "id", 1);
    String result = logDescriptionBuilder.buildMessage(READER_CREATED, reader, null, null, null);
    assertEquals("Reader with id 1 created. First name: " +
            "John. Middle name: James. Last name: Smith. Birth date: 1998-05-21.", result);
  }

  @Test
  void shouldBuildReaderCreateMessageWithMiddleNameNull(){
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    ReflectionTestUtils.setField(reader, "id", 1);
    String result = logDescriptionBuilder.buildMessage(READER_CREATED, reader, null, null, null);
    assertEquals("Reader with id 1 created. First name: " +
            "John. Last name: Smith. Birth date: 1998-05-21.", result);
  }

  @Test
  void shouldBuildBookCreateMessage(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    String result = logDescriptionBuilder.buildMessage(BOOK_CREATED, null, book, null, null);
    assertEquals(
            "Book with id 1 created. Title: The Hobbit. Author: " +
                    "J.R.R. Tolkien. Published in 1937. Published by George Allen & Unwin.", result);
  }

  @Test
  void shouldBuildBookCreateMessageWithOptionalsNull(){
    Book book = new Book("The Hobbit", null, null, null);
    ReflectionTestUtils.setField(book, "id", 1);
    String result = logDescriptionBuilder.buildMessage(BOOK_CREATED, null, book, null, null);
    assertEquals("Book with id 1 created. Title: The Hobbit.", result);
  }

  @Test
  void shouldBuildBookBorrowMessage(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    ReflectionTestUtils.setField(reader, "id", 1);
    String result = logDescriptionBuilder.buildMessage(BOOK_BORROWED, reader, book, null, null);
    assertEquals("Book The Hobbit borrowed by reader John Smith.", result);
  }

  @Test
  void shouldBuildBookReturnedMessage(){
    Book book = new Book("The Hobbit", "J.R.R. Tolkien", 1937, "George Allen & Unwin");
    ReflectionTestUtils.setField(book, "id", 1);
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    ReflectionTestUtils.setField(reader, "id", 1);
    String result = logDescriptionBuilder.buildMessage(BOOK_RETURNED, reader, book, null, null);
    assertEquals("Book The Hobbit returned by reader John Smith.", result);
  }

}
