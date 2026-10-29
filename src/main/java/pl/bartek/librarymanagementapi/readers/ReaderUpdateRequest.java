package pl.bartek.librarymanagementapi.readers;

import jakarta.validation.constraints.*;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;

public record ReaderUpdateRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "First name cannot be empty.")
        @Size(max = 100)
        JsonNullable<String> firstName,

        @Size(max = 100)
        JsonNullable<String> middleName,

        @Pattern(regexp = "(?s).*\\S.*", message = "Last name cannot be empty.")
        @Size(max = 100)
        JsonNullable<String> lastName,

        @PastOrPresent(message = "Birth date cannot be in the future.")
        JsonNullable<LocalDate> birthDate
) {
}
