package pl.bartek.librarymanagementapi.staff;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffAccountCreateRequest(
        @NotBlank
        @Size(max = 100)
        String username,

        @NotBlank
        @Size(min = 12)
        String password
) {
}
