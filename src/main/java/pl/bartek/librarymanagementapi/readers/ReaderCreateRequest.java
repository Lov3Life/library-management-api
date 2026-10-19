package pl.bartek.librarymanagementapi.readers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ReaderCreateRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String middleName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @NotNull(message = "Birth date is required")
        @PastOrPresent(message = "Birth date cannot be in the future")
        LocalDate birthDate
) {
  public static Reader toReader(ReaderCreateRequest request){
    return new Reader(request.firstName, request.middleName, request.lastName, request.birthDate);
  }
}
