package pl.bartek.librarymanagementapi.staff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffConflictException;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffInvalidParameterException;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffNotFoundException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StaffAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StaffAccountControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private StaffAccountService staffAccountService;

  @Test
  void shouldReturn200AllLibrarians() throws Exception {
    StaffAccountDTO accountDTO1 = new StaffAccountDTO(1, "librarian1", StaffAccountRole.LIBRARIAN, true);
    StaffAccountDTO accountDTO2 = new StaffAccountDTO(2, "librarian2", StaffAccountRole.LIBRARIAN, false);
    when(staffAccountService.getLibrarians())
            .thenReturn(List.of(accountDTO1, accountDTO2));
    mockMvc.perform(get("/api/staff/librarians"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].username").value("librarian1"))
            .andExpect(jsonPath("$[0].role").value("LIBRARIAN"))
            .andExpect(jsonPath("$[0].enabled").value(true))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].username").value("librarian2"))
            .andExpect(jsonPath("$[1].role").value("LIBRARIAN"))
            .andExpect(jsonPath("$[1].enabled").value(false));
  }

  @Test
  void shouldReturn200AllLibrariansEmptyList() throws Exception {
    when(staffAccountService.getLibrarians())
            .thenReturn(List.of());
    mockMvc.perform(get("/api/staff/librarians"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void shouldReturn201WhenCreateLibrarian() throws Exception {
    String request = """
            {
              "username": "librarian1",
              "password": "%s"
            }
            """.formatted("a".repeat(12));
    StaffAccountDTO response = new StaffAccountDTO(1, "librarian1", StaffAccountRole.LIBRARIAN, true);
    when(staffAccountService.createAccount(any(StaffAccountCreateRequest.class)))
            .thenReturn(response);
    mockMvc.perform(post("/api/staff")
            .contentType(MediaType.APPLICATION_JSON)
            .content(request))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").value("librarian1"))
            .andExpect(jsonPath("$.role").value("LIBRARIAN"))
            .andExpect(jsonPath("$.enabled").value(true));
  }

  @ParameterizedTest
  @ValueSource(strings = {" ", "", "\t", "\n", "\r"})
  void shouldReturn400WhenInvalidUsername(String username) throws Exception {
    String request = objectMapper.writeValueAsString(Map.of(
            "username", username,
            "password", "a".repeat(12)));
    mockMvc.perform(post("/api/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn400WhenPasswordIsTooShort() throws Exception {
    String request = """
            {
              "username": "librarian1",
              "password": "%s"
            }
            """.formatted("a".repeat(11));
    mockMvc.perform(post("/api/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn409WhenLibrarianWithUsernameExist() throws Exception {
    String request = """
            {
              "username": "librarian1",
              "password": "%s"
            }
            """.formatted("a".repeat(12));
    when(staffAccountService.createAccount(any(StaffAccountCreateRequest.class)))
            .thenThrow(new StaffConflictException("Username is already taken."));
    mockMvc.perform(post("/api/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isConflict());
  }

  @Test
  void shouldReturn400WhenPasswordIsTooLong() throws Exception {
    String request = """
            {
              "username": "librarian1",
              "password": "%s"
            }
            """.formatted("a".repeat(200));
    when(staffAccountService.createAccount(any(StaffAccountCreateRequest.class)))
            .thenThrow(new StaffInvalidParameterException("Password is too long."));
    mockMvc.perform(post("/api/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldReturn200AndEnabledTheUser() throws Exception {
    StaffAccountDTO staffAccountDTO = new StaffAccountDTO(1, "librarian", StaffAccountRole.LIBRARIAN, true);
    when(staffAccountService.enableAccount(1))
            .thenReturn(staffAccountDTO);
    mockMvc.perform(patch("/api/staff/1/enable")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("librarian"))
            .andExpect(jsonPath("$.role").value("LIBRARIAN"))
            .andExpect(jsonPath("$.enabled").value(true));
  }

  @Test
  void shouldReturn409WhenUserIsAlreadyEnabled() throws Exception {
    when(staffAccountService.enableAccount(1))
            .thenThrow(new StaffConflictException("Staff account with id: 1 is already enabled."));
    mockMvc.perform(patch("/api/staff/1/enable")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
  }

  @Test
  void shouldReturn404WhenDoesNotExistWhileEnabling() throws Exception {
    when(staffAccountService.enableAccount(1))
            .thenThrow(new StaffNotFoundException("Staff account with id: 1 not found."));
    mockMvc.perform(patch("/api/staff/1/enable")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn200AndDisabledTheUser() throws Exception {
    StaffAccountDTO staffAccountDTO = new StaffAccountDTO(1, "librarian", StaffAccountRole.LIBRARIAN, false);
    when(staffAccountService.disableAccount(1))
            .thenReturn(staffAccountDTO);
    mockMvc.perform(patch("/api/staff/1/disable")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("librarian"))
            .andExpect(jsonPath("$.role").value("LIBRARIAN"))
            .andExpect(jsonPath("$.enabled").value(false));
  }

  @Test
  void shouldReturn409WhenUserIsAlreadyDisabled() throws Exception {
    when(staffAccountService.disableAccount(1))
            .thenThrow(new StaffConflictException("Staff account with id: 1 is already disabled."));
    mockMvc.perform(patch("/api/staff/1/disable")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
  }

  @Test
  void shouldReturn409WhenUserIsAdminWhileDisabling() throws Exception {
    when(staffAccountService.disableAccount(1))
            .thenThrow(new StaffConflictException("Administrator account cannot be disabled."));
    mockMvc.perform(patch("/api/staff/1/disable")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict());
  }

  @Test
  void shouldReturn404WhenDoesNotExistWhileDisabling() throws Exception {
    when(staffAccountService.disableAccount(1))
            .thenThrow(new StaffNotFoundException("Staff account with id: 1 not found."));
    mockMvc.perform(patch("/api/staff/1/disable")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
  }
}
