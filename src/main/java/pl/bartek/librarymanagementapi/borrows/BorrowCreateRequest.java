package pl.bartek.librarymanagementapi.borrows;

import jakarta.validation.constraints.NotNull;

public record BorrowCreateRequest(@NotNull Integer bookId,
                                  @NotNull Integer readerId) {
}
