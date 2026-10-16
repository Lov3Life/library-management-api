package pl.bartek.librarymanagementapi.books;

public record BookDTO(Integer id,
                      String title,
                      String author,
                      Integer releaseYear,
                      String publishingHouse,
                      boolean active) {

  public static BookDTO fromBook(Book book) {
    return new BookDTO(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getReleaseYear(),
            book.getPublishingHouse(),
            book.isActive());
  }
}
