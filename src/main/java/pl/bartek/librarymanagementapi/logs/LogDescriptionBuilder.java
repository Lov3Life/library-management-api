package pl.bartek.librarymanagementapi.logs;

import org.springframework.stereotype.Component;
import pl.bartek.librarymanagementapi.books.Book;
import pl.bartek.librarymanagementapi.books.BookDTO;
import pl.bartek.librarymanagementapi.readers.Reader;
import pl.bartek.librarymanagementapi.readers.ReaderDTO;

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
      default -> "";
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
    if(!reader.getFirstName().equals(readerPrev.firstName()))
      massage.append(readerPrev.firstName()).append(" -> ").append(reader.getFirstName()).append(".");
    if(!reader.getMiddleName().equals(readerPrev.middleName()))
      massage.append(readerPrev.middleName()).append(" -> ").append(reader.getMiddleName()).append(".");
    if(!reader.getLastName().equals(readerPrev.lastName()))
      massage.append(readerPrev.lastName()).append(" -> ").append(reader.getLastName()).append(".");
    if(!reader.getBirthDate().equals(readerPrev.birthDate()))
      massage.append(readerPrev.birthDate()).append(" -> ").append(reader.getBirthDate()).append(".");
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
    if(!bookPrev.title().equals(book.getTitle()))
      massage.append(" ").append(bookPrev.title()).append(" -> ").append(book.getTitle()).append(".");
    if(!bookPrev.author().equals(book.getAuthor()))
      massage.append(" ").append(bookPrev.author()).append(" -> ").append(book.getAuthor()).append(".");
    if(!bookPrev.releaseYear().equals(book.getReleaseYear()))
      massage.append(" ").append(bookPrev.releaseYear()).append(" -> ").append(book.getReleaseYear()).append(".");
    if(!bookPrev.publishingHouse().equals(book.getPublishingHouse()))
      massage.append(" ").append(bookPrev.publishingHouse()).append(" -> ").append(book.getPublishingHouse()).append(".");
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
