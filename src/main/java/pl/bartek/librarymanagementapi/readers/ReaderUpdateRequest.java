package pl.bartek.librarymanagementapi.readers;

import jakarta.validation.constraints.Pattern;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.LocalDate;

public record ReaderUpdateRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "First name cannot be empty")
        JsonNullable<String> firstName,

        JsonNullable<String> middleName,

        @Pattern(regexp = "(?s).*\\S.*", message = "Last name cannot be empty")
        JsonNullable<String> lastName,

        JsonNullable<LocalDate> birthDate
) {
}
