package pl.bartek.librarymanagementapi.readers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.test.util.ReflectionTestUtils;
import pl.bartek.librarymanagementapi.borrows.BorrowRepository;
import pl.bartek.librarymanagementapi.logs.LogService;
import pl.bartek.librarymanagementapi.readers.exceptions.*;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReaderServiceTest {

  @Mock
  private ReaderRepository readerRepository;

  @Mock
  private BorrowRepository borrowRepository;

  @Mock
  private LogService logService;

  private ReaderServiceImpl readerService;

  private static final LocalDate TODAY = LocalDate.of(2026, 10, 27);

  private static final Clock FIXED_CLOCK = Clock.fixed(
          Instant.parse("2026-10-27T12:00:00Z"),
          ZoneId.of("Europe/Warsaw"));

  @BeforeEach
  void setUp() {
    readerService = new ReaderServiceImpl(readerRepository, borrowRepository, logService, FIXED_CLOCK);
  }

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
  void shouldTrimFirstNameBeforeSearching(){
    when(readerRepository.findByFirstNameContainingIgnoreCase("John"))
            .thenReturn(List.of());
    readerService.getReaders("   John   ", null);
    verify(readerRepository).findByFirstNameContainingIgnoreCase("John");
  }

  @Test
  void shouldTrimLastNameBeforeSearching(){
    when(readerRepository.findByLastNameContainingIgnoreCase("Smith"))
            .thenReturn(List.of());
    readerService.getReaders(null, "    Smith      ");
    verify(readerRepository).findByLastNameContainingIgnoreCase("Smith");
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

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void ReaderCreateInvalidParameterExceptionWhenFirstNameIsInvalid(String firstName){
    ReaderCreateRequest request = new ReaderCreateRequest(firstName, "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate());
    assertThrows(ReaderCreateInvalidParameterException.class, () -> readerService.saveReader(request));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void ReaderCreateInvalidParameterExceptionWhenLastNameIsInvalid(String lastName){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            lastName, Date.valueOf("1998-05-21").toLocalDate());
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
            "Smith", TODAY.plusDays(1));
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
  void shouldSaveReaderWhenBirthDateIsToday(){
    ReaderCreateRequest request = new ReaderCreateRequest("John", "James",
            "Smith", TODAY);
    when(readerRepository.save(any(Reader.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    ReaderDTO readerDTO = readerService.saveReader(request);
    assertEquals("John", readerDTO.firstName());
    assertEquals("James", readerDTO.middleName());
    assertEquals("Smith", readerDTO.lastName());
    assertEquals(TODAY, readerDTO.birthDate());
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

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenFirstNameIsInvalid(String firstName){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.of(firstName), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void shouldThrowReaderUpdateInvalidParameterExceptionWhenLastNameIsInvalid(String lastName){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.of(lastName), JsonNullable.undefined());
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
            JsonNullable.undefined(), JsonNullable.of(TODAY.plusDays(1)));
    when(readerRepository.findById(anyInt()))
            .thenReturn(Optional.of(new Reader()));
    assertThrows(ReaderUpdateInvalidParameterException.class, () -> readerService.updateReader(1, request));
  }

  @Test
  void shouldThrowReaderNotFoundExceptionWhenUpdatingNonExistingReader(){
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.undefined(),
            JsonNullable.undefined(), JsonNullable.undefined());
    when(readerRepository.findById(1))
            .thenReturn(Optional.empty());
    assertThrows(ReaderNotFoundException.class, () -> readerService.updateReader(1, request));
    verify(readerRepository, never()).save(any());
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
    ReaderDTO result = readerService.updateReader(1, request);
    assertEquals("Edward", result.firstName());
    assertEquals("James", result.middleName());
    assertEquals("Smith", result.lastName());
    assertEquals(LocalDate.of(1998, 5, 21), result.birthDate());
    verify(readerRepository).save(reader);
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

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  void shouldUpdateReaderMiddleNameToNullDifferentCases(String value){
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    ReaderUpdateRequest request = new ReaderUpdateRequest(JsonNullable.undefined(), JsonNullable.of(value),
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
    verify(readerRepository, never()).save(any());
  }

  @Test
  void shouldThrowExceptionWhenReaderDoesNotExistDuringActivation() {
    when(readerRepository.findById(1))
            .thenReturn(Optional.empty());
    assertThrows(ReaderNotFoundException.class, () -> readerService.activateReader(1));
    verify(readerRepository, never()).save(any());
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
    verify(readerRepository, never()).save(any());
  }

  @Test
  void shouldThrowReaderHasActiveBorrowsExceptionWhenDeactivatingReader() {
    Reader reader = new Reader("John", null,
            "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    reader.setActive(true);
    when(readerRepository.findById(1))
            .thenReturn(Optional.of(reader));
    when(borrowRepository.existsByReaderIdAndReturnDateIsNull(1))
            .thenReturn(true);
    assertThrows(ReaderHasActiveBorrowsException.class,
            () -> readerService.deactivateReader(1));
    assertTrue(reader.isActive());
    verify(borrowRepository).existsByReaderIdAndReturnDateIsNull(1);
    verify(readerRepository, never()).save(any(Reader.class));
  }

  @Test
  void shouldThrowExceptionWhenReaderDoesNotExistDuringDeactivation() {
    when(readerRepository.findById(1))
            .thenReturn(Optional.empty());
    assertThrows(ReaderNotFoundException.class, () -> readerService.deactivateReader(1));
    verify(readerRepository, never()).save(any());
  }

  @Test
  void shouldChangeReaderActiveStatusToInactive() {
    Reader reader = new Reader("John", "James", "Smith", LocalDate.of(1998, 5, 21));
    ReflectionTestUtils.setField(reader, "id", 1);
    reader.setActive(true);
    when(readerRepository.findById(1))
            .thenReturn(Optional.of(reader));
    when(borrowRepository.existsByReaderIdAndReturnDateIsNull(1))
            .thenReturn(false);
    when(readerRepository.save(any(Reader.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
    ReaderDTO result = readerService.deactivateReader(1);
    assertFalse(result.active());
    assertFalse(reader.isActive());
    verify(readerRepository).findById(1);
    verify(borrowRepository).existsByReaderIdAndReturnDateIsNull(1);
    verify(readerRepository).save(reader);
  }
}
