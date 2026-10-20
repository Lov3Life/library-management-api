package pl.bartek.librarymanagementapi.readers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.readers.exceptions.ReaderCreateInvalidParameterException;
import pl.bartek.librarymanagementapi.readers.exceptions.ReaderNotFoundException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReaderServiceTest {

  @Mock
  private ReaderRepository readerRepository;

  @InjectMocks
  private ReaderServiceImpl readerService;

  @Test
  void shouldThrowReaderNotFoundExceptionWhenReaderDoesNotExist() {
    when(readerRepository.findById(999))
            .thenReturn(Optional.empty());
    assertThrows(ReaderNotFoundException.class, () -> readerService.getReader(999));
    verify(readerRepository).findById(999);
  }

  @Test
  void shouldReturnReaderWhenExist() {
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    ReflectionTestUtils.setField(reader, "id", 1);
    when(readerRepository.findById(1))
            .thenReturn(Optional.of(reader));
    ReaderDTO readerDTO = readerService.getReader(1);
    ReaderDTO expected = new ReaderDTO(1, "John", null,
            "Smith", Date.valueOf("1998-05-21").toLocalDate(), true);
    assertEquals(expected, readerDTO);
    verify(readerRepository).findById(1);
  }

  @Test
  void shouldReturnAllReaders() {
    Reader reader1 = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    Reader reader2 = new Reader("Jane", null, "Doe", Date.valueOf("1990-01-01").toLocalDate());
    when(readerRepository.findAll())
            .thenReturn(List.of(reader1, reader2));
    List<ReaderDTO> readers = readerService.getReaders(null, null);
    assertEquals(2, readers.size());
    assertEquals("John", readers.get(0).firstName());
    assertEquals("Jane", readers.get(1).firstName());
    verify(readerRepository).findAll();
  }

  @Test
  void shouldReturnAllReadersWhenFilerByFirstName() {
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    when(readerRepository.findByFirstNameContainingIgnoreCase("John"))
            .thenReturn(List.of(reader));
    List<ReaderDTO> readers = readerService.getReaders("John", null);
    assertEquals(1, readers.size());
    assertEquals("John", readers.getFirst().firstName());
    verify(readerRepository).findByFirstNameContainingIgnoreCase("John");
  }

  @Test
  void shouldReturnAllReadersWhenFilerByFirstNameAndLastName() {
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    when(readerRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase("John", "Smith"))
            .thenReturn(List.of(reader));
    List<ReaderDTO> readers = readerService.getReaders("John", "Smith");
    assertEquals(1, readers.size());
    assertEquals("John", readers.getFirst().firstName());
    verify(readerRepository).findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase("John", "Smith");
  }

  @Test
  void shouldReturnAllReadersWhenFilterByLastName(){
    Reader reader = new Reader("John", null, "Smith", Date.valueOf("1998-05-21").toLocalDate());
    when(readerRepository.findByLastNameContainingIgnoreCase("Smith"))
            .thenReturn(List.of(reader));
    List<ReaderDTO> readers = readerService.getReaders(null, "Smith");
    assertEquals(1, readers.size());
    assertEquals("Smith", readers.getFirst().lastName());
    verify(readerRepository).findByLastNameContainingIgnoreCase("Smith");
  }

  @Test
  void shouldThrowReaderCreateInvalidParameterExceptionWhenFirstNameIsNullReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest(null, "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate());
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldThrowReaderCreateInvalidParameterExceptionWhenFirstNameIsEmptyReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest("", "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate());
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldThrowReaderCreateInvalidParameterExceptionWhenFirstNameIsBlankReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest("      ", "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate());
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldThrowReaderCreateInvalidParameterExceptionWhenLastNameIsNullReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            null, Date.valueOf("1998-05-21").toLocalDate());
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldThrowReaderCreateInvalidParameterExceptionWhenLastNameIsEmptyReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            "", Date.valueOf("1998-05-21").toLocalDate());
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldThrowReaderCreateInvalidParameterExceptionWhenLastNameIsBlankReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            "      ", Date.valueOf("1998-05-21").toLocalDate());
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldThrowReaderCreateInvalidParameterExceptionWhenBirthDateIsNullReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            "Smith", null);
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldSaveReaderWhenAllParametersAreValid(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate());
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(new Reader("John", "James",
                    "Smith", LocalDate.of(1998, 5, 21)));
    ReaderDTO readerDTO = readerService.saveReader(request);
    assertEquals("John", readerDTO.firstName());
    assertEquals("James", readerDTO.middleName());
    assertEquals("Smith", readerDTO.lastName());
    assertEquals(LocalDate.of(1998, 5, 21), readerDTO.birthDate());
  }
}
