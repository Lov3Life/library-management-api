package pl.bartek.librarymanagementapi.books;

import jakarta.validation.constraints.Pattern;
import org.openapitools.jackson.nullable.JsonNullable;

public record BookUpdateRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "Book title cannot be empty")
        String title,
        JsonNullable<String> author,
        JsonNullable<Integer> releaseYear,
        JsonNullable<String> publishingHouse) {
}