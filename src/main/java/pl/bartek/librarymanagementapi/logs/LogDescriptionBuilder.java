package pl.bartek.librarymanagementapi.logs;

import org.springframework.stereotype.Component;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookDTO;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderDTO;

import java.util.Objects;

@Component
public class LogDescriptionBuilder {

  public String buildMessage(LogEventType logEventType,
                             Reader reader, Book book,
                             ReaderDTO readerPrev, BookDTO bookPrev) {
    return switch(logEventType) {
      case BOOK_CREATED -> bookCreate(book);
      case BOOK_UPDATED -> bookUpdate(book, bookPrev);
      case BOOK_ACTIVATED -> bookActivate(book);
      case BOOK_DEACTIVATED -> bookDeactivate(book);
      case READER_CREATED -> readerCreate(reader);
      case READER_UPDATED -> readerUpdate(reader, readerPrev);
      case READER_ACTIVATED -> readerActivate(reader);
      case READER_DEACTIVATED -> readerDeactivate(reader);
      case BOOK_BORROWED -> bookBorrow(reader, book);
      case BOOK_RETURNED -> bookReturn(reader, book);
    };
  }

  private String bookReturn(Reader reader, Book book) {
    return "Book " + book.getTitle() + " returned by reader " + reader.getFirstName() + " " + reader.getLastName();
  }

  private String bookBorrow(Reader reader, Book book) {
    return "Book " + book.getTitle() + " borrowed by reader " + reader.getFirstName() + " " + reader.getLastName();
  }

  private String readerDeactivate(Reader reader) {
    return "Reader " + reader.getFirstName() + " " + reader.getLastName() + " deactivated.";
  }

  private String readerActivate(Reader reader) {
    return "Reader " + reader.getFirstName() + " " + reader.getLastName() + " activated.";
  }

  private String readerUpdate(Reader reader, ReaderDTO readerPrev) {
    StringBuilder massage = new StringBuilder();
    massage.append("Reader with id ").append(reader.getId()).append(" updated.");
    if(!Objects.equals(readerPrev.firstName(), reader.getFirstName()))
      massage.append(" FirstName: ").append(readerPrev.firstName()).append(" -> ").append(reader.getFirstName()).append(".");
    if(!Objects.equals(readerPrev.middleName(), reader.getMiddleName()))
      massage.append(" MiddleName: ").append(readerPrev.middleName()).append(" -> ").append(reader.getMiddleName()).append(".");
    if(!Objects.equals(readerPrev.lastName(), reader.getLastName()))
      massage.append(" LastName: ").append(readerPrev.lastName()).append(" -> ").append(reader.getLastName()).append(".");
    if(!Objects.equals(readerPrev.birthDate(), reader.getBirthDate()))
      massage.append(" BirthDate: ").append(readerPrev.birthDate()).append(" -> ").append(reader.getBirthDate()).append(".");
    return massage.toString();
  }

  private String readerCreate(Reader reader) {
    StringBuilder massage = new StringBuilder();
    massage.append("Reader with id ").append(reader.getId()).append(" created.");
    if(reader.getFirstName() != null)
      massage.append(" First name: ").append(reader.getFirstName()).append(".");
    if(reader.getMiddleName() != null)
      massage.append(" Middle name: ").append(reader.getMiddleName()).append(".");
    if(reader.getLastName() != null)
      massage.append(" Last name: ").append(reader.getLastName()).append(".");
    if(reader.getBirthDate() != null)
      massage.append(" Birth date: ").append(reader.getBirthDate()).append(".");
    return massage.toString();
  }


  private String bookDeactivate(Book book) {
    return "Book " + book.getTitle() + " deactivated.";
  }

  private String bookActivate(Book book) {
    return "Book " + book.getTitle() + " activated.";
  }

  private String bookUpdate(Book book, BookDTO bookPrev) {
    StringBuilder massage = new StringBuilder();
    massage.append("Book updated.");
    if(!Objects.equals(bookPrev.title(), book.getTitle()))
      massage.append(" Title: ").append(bookPrev.title()).append(" -> ").append(book.getTitle()).append(".");
    if(!Objects.equals(bookPrev.author(), book.getAuthor()))
      massage.append(" Author: ").append(bookPrev.author()).append(" -> ").append(book.getAuthor()).append(".");
    if(!Objects.equals(bookPrev.releaseYear(), book.getReleaseYear()))
      massage.append(" ReleaseYear: ").append(bookPrev.releaseYear()).append(" -> ").append(book.getReleaseYear()).append(".");
    if(!Objects.equals(bookPrev.publishingHouse(), book.getPublishingHouse()))
      massage.append(" PublishingHouse: ").append(bookPrev.publishingHouse()).append(" -> ").append(book.getPublishingHouse()).append(".");
    return massage.toString();
  }

  private String bookCreate(Book book) {
    StringBuilder massage = new StringBuilder();
    massage.append("Book ").append(book.getTitle()).append(" created. ID ").append(book.getId());
    if(book.getAuthor() != null)
      massage.append(". Author: ").append(book.getAuthor());
    if(book.getReleaseYear() != null)
      massage.append(". Published in ").append(book.getReleaseYear());
    if(book.getPublishingHouse() != null)
      massage.append(". Published by ").append(book.getPublishingHouse());
    massage.append(".");
    return massage.toString();
  }
}
