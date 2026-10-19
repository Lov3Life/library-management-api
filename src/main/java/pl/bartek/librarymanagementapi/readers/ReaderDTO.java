package pl.bartek.librarymanagementapi.readers;

import java.time.LocalDate;

public record ReaderDTO(Integer id,
                        String firstName,
                        String middleName,
                        String lastName,
                        LocalDate birthDate,
                        boolean active) {
  public static ReaderDTO fromReader(Reader reader){
    return new ReaderDTO(
            reader.getId(),
            reader.getFirstName(),
            reader.getMiddleName(),
            reader.getLastName(),
            reader.getBirthDate(),
            reader.isActive());
  }
}
