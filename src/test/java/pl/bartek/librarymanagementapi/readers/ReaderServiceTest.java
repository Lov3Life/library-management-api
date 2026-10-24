package pl.bartek.librarymanagementapi.readers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.readers.exceptions.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
  void shouldThrowReaderCreateInvalidParameterExceptionWhenBirthDateIsInTheFutureReaderSaveRequest(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            "Smith", LocalDate.now().plusMonths(1));
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @Test
  void shouldSaveReaderWhenAllParametersAreValid(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate());
    when(readerRepository.save(any(Reader.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    ReaderDTO readerDTO = readerService.saveReader(request);
    assertEquals("John", readerDTO.firstName());
    assertEquals("James", readerDTO.middleName());
    assertEquals("Smith", readerDTO.lastName());
    assertEquals(LocalDate.of(1998, 5, 21), readerDTO.birthDate());
  }

  @Test
  void shouldSaveReaderAndNormalizeParameters(){
    ReaderCreateRequest request = new ReaderCreateRequest(" John    ", "  James     ",
            "     Smith   ", Date.valueOf("1998-05-21").toLocalDate());
    when(readerRepository.save(any(Reader.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    ReaderDTO readerDTO = readerService.saveReader(request);
    assertEquals("John", readerDTO.firstName());
    assertEquals("James", readerDTO.middleName());
    assertEquals("Smith", readerDTO.lastName());
    assertEquals(LocalDate.of(1998, 5, 21), readerDTO.birthDate());
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenFirstNameIsNull(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of(null), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenFirstNameIsEmpty(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of(""), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenFirstNameIsBlank(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of("       "), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenLastNameIsNull(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.of(null), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenLastNameIsEmpty(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.of(""), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenLastNameIsBlank(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.of("      "), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenDateIsNull(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.of(null));
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenDateIsInTheFuture(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.of(LocalDate.now().plusMonths(1)));
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldUpdateReaderOnlyFirstName (){
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of("Edward"), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    ReaderDTO readerDTOUpdated = readerService.updateReader(1, request);
    ReaderDTO readerDTO = ReaderDTO.fromReader(reader);
    assertEquals(readerDTO, readerDTOUpdated);
  }

  @Test
  void shouldLeaveReaderUnchangedWhenAllFieldsUndefined(){
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    ReaderDTO readerDTOUpdated = readerService.updateReader(1, request);
    ReaderDTO readerDTO = ReaderDTO.fromReader(reader);
    assertEquals(readerDTO, readerDTOUpdated);
  }

  @Test
  void shouldUpdateReaderOnlyFirstNameWithNormalization(){
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of(" Edward    "), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    readerService.updateReader(1, request);
    assertEquals("Edward", reader.getFirstName());
  }

  @Test
  void shouldUpdateReaderAllFields(){
    Reader reader = new Reader("John", null, "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of("Edward"), JsonNullable.of("James"),
            JsonNullable.of("Kenway"), JsonNullable.of(LocalDate.of(1693, 3, 10)));
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    readerService.updateReader(1, request);
    assertEquals("Edward", reader.getFirstName());
    assertEquals("James", reader.getMiddleName());
    assertEquals("Kenway", reader.getLastName());
    assertEquals(LocalDate.of(1693, 3, 10), reader.getBirthDate());
  }

  @Test
  void shouldUpdateReaderAllFieldsWithNormalization(){
    Reader reader = new Reader("John", null, "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of("   Edward  "), JsonNullable.of("   James  "),
            JsonNullable.of(" Kenway "), JsonNullable.of(LocalDate.of(1693, 3, 10)));
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    readerService.updateReader(1, request);
    assertEquals("Edward", reader.getFirstName());
    assertEquals("James", reader.getMiddleName());
    assertEquals("Kenway", reader.getLastName());
    assertEquals(LocalDate.of(1693, 3, 10), reader.getBirthDate());
  }

  @Test
  void shouldUpdateReaderWhenOptionalFieldsAreNull(){
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.of(null),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    readerService.updateReader(1, request);
    assertEquals("John", reader.getFirstName());
    assertNull(reader.getMiddleName());
    assertEquals("Smith", reader.getLastName());
    assertEquals(LocalDate.of(1998, 5, 21), reader.getBirthDate());
  }

  @Test
  void shouldUpdateReaderWhenOptionalFieldsAreEmpty(){
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.of(""),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    readerService.updateReader(1, request);
    assertEquals("John", reader.getFirstName());
    assertNull(reader.getMiddleName());
    assertEquals("Smith", reader.getLastName());
    assertEquals(LocalDate.of(1998, 5, 21), reader.getBirthDate());
  }

  @Test
  void shouldUpdateReaderWhenOptionalFieldsAreBlank(){
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.of("     "),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenReturn(reader);
    readerService.updateReader(1, request);
    assertEquals("John", reader.getFirstName());
    assertNull(reader.getMiddleName());
    assertEquals("Smith", reader.getLastName());
    assertEquals(LocalDate.of(1998, 5, 21), reader.getBirthDate());
  }

  @Test
  void shouldThrowExceptionWhenReaderIsAlreadyActive() {
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    reader.setActive(true);
    when(readerRepository.findById(1))
            .thenReturn(Optional.of(reader));
    assertThrows(ReaderAlreadyActiveException.class, () -> readerService.activateReader(1));
    verify(readerRepository).findById(1);
  }

  @Test
  void shouldChangeReaderActiveStatusToActive() {
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    reader.setActive(false);
    when(readerRepository.findById(1))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    ReaderDTO result = readerService.activateReader(1);
    assertTrue(result.active());
    verify(readerRepository).findById(1);
    verify(readerRepository).save(reader);
  }

  @Test
  void shouldThrowExceptionWhenReaderIsAlreadyInactive() {
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    reader.setActive(false);
    when(readerRepository.findById(1))
            .thenReturn(Optional.of(reader));
    assertThrows(ReaderAlreadyInactiveException.class, () -> readerService.deactivateReader(1));
    verify(readerRepository).findById(1);
  }

  @Test
  void shouldChangeReaderActiveStatusToInactive() {
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    reader.setActive(true);
    when(readerRepository.findById(1))
            .thenReturn(Optional.of(reader));
    when(readerRepository.save(any(Reader.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    ReaderDTO result = readerService.deactivateReader(1);
    assertFalse(result.active());
    verify(readerRepository).findById(1);
    verify(readerRepository).save(reader);
  }
}
