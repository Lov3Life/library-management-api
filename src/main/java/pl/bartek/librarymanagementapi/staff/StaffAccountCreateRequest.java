package pl.bartek.librarymanagementapi.staff;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StaffAccountCreateRequest(
        @NotNull
        @Size(max = 100)
        String username,

        @NotNull
        @Size(min = 12)
        String password
) {
}
