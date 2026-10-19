package pl.bartek.librarymanagementapi.readers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record ReaderCreateRequest(
        @NotBlank(message = "First name is required")
        String firstName,
        String middleName,
        @NotBlank(message = "Last name is required")
        String lastName,
        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date cannot be in the future")
        LocalDate birthDate
) {
  public static Reader toReader(ReaderCreateRequest request){
    return new Reader(request.firstName, request.middleName, request.lastName, request.birthDate);
  }
}
