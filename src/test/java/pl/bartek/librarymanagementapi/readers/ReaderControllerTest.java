package pl.bartek.librarymanagementapi.readers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.readers.exceptions.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReaderController.class)
public class ReaderControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ReaderService readerService;

  @Test
  void shouldReturn404WhenReaderDoesNotExist() throws Exception {
    when(readerService.getReader(anyInt()))
            .thenThrow(new ReaderNotFoundException(1));
    mockMvc.perform(get("/api/readers/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Reader with id: 1 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn200WhenReaderExists() throws Exception {
    ReaderDTO readerDTO = new ReaderDTO(1, "John", "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate(), true);
    when(readerService.getReader(1))
            .thenReturn(readerDTO);
    mockMvc.perform(get("/api/readers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Smith"))
            .andExpect(jsonPath("$.birthDate").value("1998-05-21"))
            .andExpect(jsonPath("$.active").value(true));
    verify(readerService).getReader(1);
  }

  @Test
  void shouldReturn200WhenReturnAllBook() throws Exception {
    ReaderDTO readerDTO1 = new ReaderDTO(1, "John", null,
            "Smith", Date.valueOf("1998-05-21").toLocalDate(), true);
    ReaderDTO readerDTO2 = new ReaderDTO(2, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.getReaders(null, null))
            .thenReturn(List.of(readerDTO1, readerDTO2));
    mockMvc.perform(get("/api/readers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[0].lastName").value("Smith"))
            .andExpect(jsonPath("$[0].birthDate").value("1998-05-21"))
            .andExpect(jsonPath("$[0].active").value(true))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Edward"))
            .andExpect(jsonPath("$[1].lastName").value("Kenway"))
            .andExpect(jsonPath("$[1].birthDate").value("1693-03-10"))
            .andExpect(jsonPath("$[1].active").value(true));
    verify(readerService).getReaders(null, null);
  }

  @Test
  void shouldReturn200WhenEmptyArray() throws Exception {
    when(readerService.getReaders(null, null))
            .thenReturn(List.of());
    mockMvc.perform(get("/api/readers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    verify(readerService).getReaders(null, null);
  }

  @Test
  void shouldReturn200WhenFirstNameMatches() throws Exception {
    ReaderDTO readerDTO = new ReaderDTO(2, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.getReaders("Edward", null))
            .thenReturn(List.of(readerDTO));
    mockMvc.perform(get("/api/readers?firstName=Edward"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].firstName").value("Edward"))
            .andExpect(jsonPath("$[0].lastName").value("Kenway"))
            .andExpect(jsonPath("$[0].birthDate").value("1693-03-10"))
            .andExpect(jsonPath("$[0].active").value(true));
    verify(readerService).getReaders("Edward", null);
  }

  @Test
  void shouldReturn200WhenLastNameMatches() throws Exception {
    ReaderDTO readerDTO = new ReaderDTO(2, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.getReaders(null, "Kenway"))
            .thenReturn(List.of(readerDTO));
    mockMvc.perform(get("/api/readers?lastName=Kenway"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].firstName").value("Edward"))
            .andExpect(jsonPath("$[0].lastName").value("Kenway"))
            .andExpect(jsonPath("$[0].birthDate").value("1693-03-10"))
            .andExpect(jsonPath("$[0].active").value(true));
    verify(readerService).getReaders(null, "Kenway");
  }

  @Test
  void shouldReturn200WhenFirstNameAndLastNameMatches() throws Exception {
    ReaderDTO readerDTO = new ReaderDTO(2, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.getReaders("Edward", "Kenway"))
            .thenReturn(List.of(readerDTO));
    mockMvc.perform(get("/api/readers?firstName=Edward&lastName=Kenway"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(2))
            .andExpect(jsonPath("$[0].firstName").value("Edward"))
            .andExpect(jsonPath("$[0].lastName").value("Kenway"))
            .andExpect(jsonPath("$[0].birthDate").value("1693-03-10"))
            .andExpect(jsonPath("$[0].active").value(true));
    verify(readerService).getReaders("Edward", "Kenway");
  }

  @Test
  void shouldReturn201WhenReaderCreateSuccessfully() throws Exception {
    String request = """
            {
              "firstName": "Edward",
              "middleName": "James",
              "lastName": "Kenway",
              "birthDate": "1693-03-10"
            }
            """;
    ReaderDTO response = new ReaderDTO(1, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.saveReader(any(ReaderCreateRequest.class)))
            .thenReturn(response);
    mockMvc.perform(post("/api/readers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Edward"))
            .andExpect(jsonPath("$.middleName").value("James"))
            .andExpect(jsonPath("$.lastName").value("Kenway"))
            .andExpect(jsonPath("$.birthDate").value("1693-03-10"))
            .andExpect(jsonPath("$.active").value(true));
    verify(readerService).saveReader(any(ReaderCreateRequest.class));
  }

  @Test
  void shouldReturn201WhenReaderCreateSuccessfullyWithoutMiddleNameNull() throws Exception {
    String request = """
            {
              "firstName": "Edward",
              "lastName": "Kenway",
              "birthDate": "1693-03-10"
            }
            """;
    ReaderDTO response = new ReaderDTO(1, "Edward", null,
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.saveReader(any(ReaderCreateRequest.class)))
            .thenReturn(response);
    mockMvc.perform(post("/api/readers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Edward"))
            .andExpect(jsonPath("$.lastName").value("Kenway"))
            .andExpect(jsonPath("$.birthDate").value("1693-03-10"))
            .andExpect(jsonPath("$.active").value(true));
    verify(readerService).saveReader(any(ReaderCreateRequest.class));
  }

  @Test
  void shouldReturn201WhenReaderCreateSuccessfullyWithTodayBirthDate() throws Exception {
    String request = """
            {
              "firstName": "Edward",
              "middleName": "James",
              "lastName": "Kenway",
              "birthDate": "%s"
            }
            """.formatted(LocalDate.now());
    ReaderDTO response = new ReaderDTO(1, "Edward", "James",
            "Kenway", LocalDate.now(), true);
    when(readerService.saveReader(any(ReaderCreateRequest.class)))
            .thenReturn(response);
    mockMvc.perform(post("/api/readers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Edward"))
            .andExpect(jsonPath("$.middleName").value("James"))
            .andExpect(jsonPath("$.lastName").value("Kenway"))
            .andExpect(jsonPath("$.birthDate").value(LocalDate.now().toString()))
            .andExpect(jsonPath("$.active").value(true));
    verify(readerService).saveReader(any(ReaderCreateRequest.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {
          """
          {
              "lastName": "Kenway",
              "birthDate": "1693-03-10"
          }
          """,
          """
          {
            "firstName": "",
            "lastName": "Kenway",
            "birthDate": "1693-03-10"
          }
          """,
          """
          {
            "firstName": "           ",
            "lastName": "Kenway",
            "birthDate": "1693-03-10"
          }
          """,
          """
          {
            "firstName": "Edward",
            "birthDate": "1693-03-10"
          }
          """,
          """
          {
            "firstName": "Edward",
            "lastName": "",
            "birthDate": "1693-03-10"
          }
          """,
          """
          {
            "firstName": "Edward",
            "lastName": "      ",
            "birthDate": "1693-03-10"
          }
          """,
          """
          {
            "firstName": "Edward",
            "lastName": "Kenway"
          }
          """,
  })
  void shouldReturn400WhenReaderCreateRequestIsInvalid(String request) throws Exception{
    mockMvc.perform(post("/api/readers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    verifyNoInteractions(readerService);
  }

  @Test
  void shouldReturn400WhenReaderCreateRequestHasBirthDateInFuture() throws Exception {
    String request = """
            {
              "firstName": "Edward",
              "lastName": "Kenway",
              "birthDate": "%s"
            }
            """.formatted(LocalDate.now().plusDays(1));
    mockMvc.perform(post("/api/readers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    verifyNoInteractions(readerService);
  }

  @Test
  void shouldReturn200WhenReaderUpdateSuccessfullyWithEmptyBody() throws Exception {
    String request = """
            {}
            """;
    ReaderDTO response = new ReaderDTO(1, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.updateReader(anyInt(), any(ReaderUpdateRequest.class)))
            .thenReturn(response);
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk());
    ArgumentCaptor<ReaderUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ReaderUpdateRequest.class);
    verify(readerService).updateReader(eq(1), requestCaptor.capture());
    ReaderUpdateRequest capturedRequest = requestCaptor.getValue();
    assertFalse(capturedRequest.firstName().isPresent());
    assertFalse(capturedRequest.middleName().isPresent());
    assertFalse(capturedRequest.lastName().isPresent());
    assertFalse(capturedRequest.birthDate().isPresent());
  }

  @Test
  void shouldReturn200WhenReaderUpdateSuccessfully() throws Exception {
    String request = """
            {
              "firstName": "Edward",
              "lastName": "Kenway",
              "birthDate": "1693-03-10"
            }
            """;
    ReaderDTO response = new ReaderDTO(1, "Edward", "James",
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.updateReader(anyInt(), any(ReaderUpdateRequest.class)))
            .thenReturn(response);
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk());
    ArgumentCaptor<ReaderUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ReaderUpdateRequest.class);
    verify(readerService).updateReader(eq(1), requestCaptor.capture());
    ReaderUpdateRequest capturedRequest = requestCaptor.getValue();
    assertEquals("Edward", capturedRequest.firstName().get());
    assertEquals("Kenway", capturedRequest.lastName().get());
    assertEquals(LocalDate.of(1693, 3, 10), capturedRequest.birthDate().get());
  }

  @Test
  void shouldReturn200WhenReaderUpdateSuccessfullyWithMiddleNameNull() throws Exception {
    String request = """
            {
              "middleName": null
            }
            """;
    ReaderDTO response = new ReaderDTO(1, "Edward", null,
            "Kenway", LocalDate.of(1693, 3, 10), true);
    when(readerService.updateReader(anyInt(), any(ReaderUpdateRequest.class)))
            .thenReturn(response);
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isOk());
    ArgumentCaptor<ReaderUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ReaderUpdateRequest.class);
    verify(readerService).updateReader(eq(1), requestCaptor.capture());
    ReaderUpdateRequest capturedRequest = requestCaptor.getValue();
    assertEquals(JsonNullable.of(null), capturedRequest.middleName());
  }

  @Test
  void shouldReturn404WhenReaderUpdateRequestIdIsNotFound() throws Exception{
    String request = """
            {
              "firstName": "Edward",
              "lastName": "Kenway",
              "birthDate": "1693-03-10"
            }
            """;
    when(readerService.updateReader(anyInt(), any(ReaderUpdateRequest.class)))
            .thenThrow(new ReaderNotFoundException(2));
    mockMvc.perform(patch("/api/readers/2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Reader with id: 2 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn400WhenReaderUpdateRequestFirstNameIsNull() throws Exception{
    String request = """
            {
              "firstName": null
            }
            """;
    when(readerService.updateReader(anyInt(), any(ReaderUpdateRequest.class)))
            .thenThrow(new ReaderUpdateInvalidParameterException(
                    "Reader first name cannot be blank or Null. If change is no needed, don't fill param."));
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    ArgumentCaptor<ReaderUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ReaderUpdateRequest.class);
    verify(readerService).updateReader(eq(1), requestCaptor.capture());
    ReaderUpdateRequest capturedRequest = requestCaptor.getValue();
    assertNull(capturedRequest.firstName().get());
  }

  @ParameterizedTest
  @ValueSource(strings = {"firstName", "lastName", "middleName"})
  void shouldReturn400WhenReaderUpdateRequestValueIsTooLong(String param) throws Exception{
    String request = """
            {
              "%s": "%s"
            }
            """.formatted(param, "a".repeat(101));
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    verify(readerService, never()).updateReader(eq(1), any(ReaderUpdateRequest.class));
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "\"firstName\":\"\"",
          "\"lastName\":\"\"",
          "\"firstName\":\"    \"",
          "\"lastName\":\"       \""})
  void shouldReturn400WhenReaderUpdateRequestNotMatchPattern(String value) throws Exception{
    String request = """
            {
              %s
            }
            """.formatted(value);
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    verify(readerService, never()).updateReader(eq(1), any(ReaderUpdateRequest.class));
  }

  @Test
  void shouldReturn400WhenReaderUpdateRequestLastNameIsNull() throws Exception{
    String request = """
            {
              "lastName": null
            }
            """;
    when(readerService.updateReader(anyInt(), any(ReaderUpdateRequest.class)))
            .thenThrow(new ReaderUpdateInvalidParameterException(
                    "Reader last name cannot be blank or Null. If change is no needed, don't fill param."));
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    ArgumentCaptor<ReaderUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ReaderUpdateRequest.class);
    verify(readerService).updateReader(eq(1), requestCaptor.capture());
    ReaderUpdateRequest capturedRequest = requestCaptor.getValue();
    assertNull(capturedRequest.lastName().get());
  }

  @Test
  void shouldReturn400WhenReaderUpdateRequestBirthDateIsNull() throws Exception{
    String request = """
            {
              "birthDate": null
            }
            """;
    when(readerService.updateReader(anyInt(), any(ReaderUpdateRequest.class)))
            .thenThrow(new ReaderUpdateInvalidParameterException(
                    "Reader birth date cannot be blank or Null. If change is no needed, don't fill param."));
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    ArgumentCaptor<ReaderUpdateRequest> requestCaptor = ArgumentCaptor.forClass(ReaderUpdateRequest.class);
    verify(readerService).updateReader(eq(1), requestCaptor.capture());
    ReaderUpdateRequest capturedRequest = requestCaptor.getValue();
    assertNull(capturedRequest.birthDate().get());
  }

  @Test
  void shouldReturn400WhenReaderUpdateRequestBirthDateIsInTheFuture() throws Exception{
    String future = LocalDate.now().plusMonths(1).toString();
    String request = """
            {
              "birthDate": "%s"
            }
            """.formatted(future);
    mockMvc.perform(patch("/api/readers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
    verify(readerService, never()).updateReader(eq(1), any(ReaderUpdateRequest.class));
  }

  @Test
  void shouldReturn200WhenChangingReaderStatusToActive() throws Exception {
    ReaderDTO readerDTO = new ReaderDTO(1, "John", "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate(), true);
    when(readerService.activateReader(1))
            .thenReturn(readerDTO);
    mockMvc.perform(patch("/api/readers/1/activate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.active").value(true));
    verify(readerService).activateReader(1);
  }

  @Test
  void shouldReturn409WhenChangingReaderStatusToActive() throws Exception {
    when(readerService.activateReader(1))
            .thenThrow(new ReaderAlreadyActiveException(1));
    mockMvc.perform(patch("/api/readers/1/activate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Reader with id 1 is already active."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn400WhenChangingReaderStatusToActiveAndReaderDoesNotExist() throws Exception {
    when(readerService.activateReader(1))
            .thenThrow(new ReaderNotFoundException(1));
    mockMvc.perform(patch("/api/readers/1/activate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Reader with id: 1 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn200WhenChangingReaderStatusToInactive() throws Exception {
    ReaderDTO readerDTO = new ReaderDTO(1, "John", "James",
            "Smith", Date.valueOf("1998-05-21").toLocalDate(), false);
    when(readerService.deactivateReader(1))
            .thenReturn(readerDTO);
    mockMvc.perform(patch("/api/readers/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.active").value(false));
    verify(readerService).deactivateReader(1);
  }

  @Test
  void shouldReturn409WhenChangingReaderStatusToInactive() throws Exception {
    when(readerService.deactivateReader(1))
            .thenThrow(new ReaderAlreadyInactiveException(1));
    mockMvc.perform(patch("/api/readers/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.detail").value("Reader with id 1 is already Inactive."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }

  @Test
  void shouldReturn409WhenDeactivatingReaderWithActiveBorrows() throws Exception {
    when(readerService.deactivateReader(1))
            .thenThrow(ReaderHasActiveBorrowsException.class);
    mockMvc.perform(patch("/api/readers/1/deactivate"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.trace").doesNotExist());
    verify(readerService).deactivateReader(1);
  }

  @Test
  void shouldReturn400WhenChangingReaderStatusToInactiveAndReaderDoesNotExist() throws Exception {
    when(readerService.deactivateReader(1))
            .thenThrow(new ReaderNotFoundException(1));
    mockMvc.perform(patch("/api/readers/1/deactivate")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("Reader with id: 1 not found."))
            .andExpect(jsonPath("$.trace").doesNotExist());
  }
}