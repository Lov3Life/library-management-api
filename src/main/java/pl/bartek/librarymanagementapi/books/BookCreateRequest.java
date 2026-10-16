package pl.bartek.librarymanagementapi.books;

import jakarta.validation.constraints.NotBlank;

public record BookCreateRequest(
        @NotBlank(message = "Book title is required")
        String title,
        String author,
        Integer releaseYear,
        String publishingHouse
) {
  public static Book toBook(BookCreateRequest request){
    return new Book(request.title, request.author, request.releaseYear, request.publishingHouse);
  }
}
