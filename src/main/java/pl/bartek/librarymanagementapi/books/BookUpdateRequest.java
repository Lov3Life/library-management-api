package pl.bartek.librarymanagementapi.books;

import jakarta.validation.constraints.Pattern;

public record BookUpdateRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "Book title cannot be empty")
        String title,
        String author,
        Integer releaseYear,
        String publishingHouse) {
}