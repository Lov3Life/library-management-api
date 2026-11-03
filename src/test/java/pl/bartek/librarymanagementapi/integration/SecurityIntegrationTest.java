package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.integration.config.MySqlTestConfiguration;
import pl.bartek.librarymanagementapi.staff.StaffAccount;
import pl.bartek.librarymanagementapi.staff.StaffAccountRepository;
import pl.bartek.librarymanagementapi.staff.StaffAccountRole;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(MySqlTestConfiguration.class)
public class SecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private StaffAccountRepository staffAccountRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    staffAccountRepository.deleteAll();
    StaffAccount admin = new StaffAccount("admin-test",
            passwordEncoder.encode("AdminPassword123!"),
            StaffAccountRole.ADMIN);
    StaffAccount librarian = new StaffAccount("librarian-test",
            passwordEncoder.encode("LibrarianPassword123!"),
            StaffAccountRole.LIBRARIAN);
    staffAccountRepository.saveAll(List.of(admin, librarian));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/"})
  void should200WhenRequestIsAnonymous(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint))
            .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/books", "/api/readers", "/api/borrows", "/api/staff/librarians", "/api/logs", "/api/other"})
  void should401WhenRequestIsAnonymous(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Authentication is required or credentials are invalid."));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/books", "/api/readers", "/api/borrows"})
  void should200WhenRequestIsLibrarian(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("librarian-test", "LibrarianPassword123!")))
            .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/staff/librarians", "/api/logs", "/api/other"})
  void should403WhenRequestIsLibrarian(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("librarian-test", "LibrarianPassword123!")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource."));
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/books", "/api/readers", "/api/borrows", "/api/staff/librarians", "/api/logs"})
  void should200WhenRequestIsAdmin(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("admin-test", "AdminPassword123!")))
            .andExpect(status().isOk());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/other"})
  void should403WhenRequestIsAdmin(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("admin-test", "AdminPassword123!")))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.detail").value("You do not have permission to access this resource."));
  }

  @Test
  void should401WhenPasswordIsWrong() throws Exception {
    mockMvc.perform(get("/api/books")
                    .with(httpBasic("librarian-test", "WrongPassword")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Authentication is required or credentials are invalid."));
  }

  @Test
  void should401WhenUsernameIsNotExist() throws Exception {
    mockMvc.perform(get("/api/books")
                    .with(httpBasic("librarian-test123", "LibrarianPassword123!")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Authentication is required or credentials are invalid."));
  }

  @Test
  void should401WhenLibrarianIsDisabled() throws Exception {
    StaffAccount librarian = staffAccountRepository.findByUsername("librarian-test").orElseThrow(RuntimeException::new);
    librarian.setEnabled(false);
    staffAccountRepository.save(librarian);
    mockMvc.perform(get("/api/books")
                    .with(httpBasic("librarian-test", "LibrarianPassword123!")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Authentication is required or credentials are invalid."));
  }

  @Test
  void should201WhenAdminCreateLibrarian() throws Exception {
    String request = """
            {
              "username": "librarian1",
              "password": "strongPassword123123"
            }
            """;
    mockMvc.perform(post("/api/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .with(httpBasic("admin-test", "AdminPassword123!")))
            .andExpect(status().isCreated());
    List<StaffAccount> librarianAccounts = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN);
    assertEquals(2, librarianAccounts.size());
  }

  @Test
  void shouldThrow403WhenLibrarianCreateLibrarian() throws Exception {
    String request = """
            {
              "username": "librarian1",
              "password": "strongPassword123123"
            }
            """;
    mockMvc.perform(post("/api/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                    .with(httpBasic("librarian-test", "LibrarianPassword123!")))
            .andExpect(status().isForbidden());
    List<StaffAccount> librarianAccounts = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN);
    assertEquals(1, librarianAccounts.size());
  }

  @Test
  void shouldThrow401WhenAnonymousCreateLibrarian() throws Exception {
    String request = """
            {
              "username": "librarian1",
              "password": "strongPassword123123"
            }
            """;
    mockMvc.perform(post("/api/staff")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
            .andExpect(status().isUnauthorized());
    List<StaffAccount> librarianAccounts = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN);
    assertEquals(1, librarianAccounts.size());
  }

  @Test
  void shouldDisableLibrarianAsAdmin() throws Exception {
    StaffAccount librarian = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN).getFirst();
    mockMvc.perform(patch("/api/staff/{id}/disable", librarian.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(httpBasic("admin-test", "AdminPassword123!")))
            .andExpect(status().isOk());
    librarian = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN).getFirst();
    assertFalse(librarian.isEnabled());
  }

  @Test
  void shouldEnableLibrarianAsAdmin() throws Exception {
    StaffAccount librarian = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN).getFirst();
    librarian.setEnabled(false);
    staffAccountRepository.save(librarian);
    mockMvc.perform(patch("/api/staff/{id}/enable", librarian.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(httpBasic("admin-test", "AdminPassword123!")))
            .andExpect(status().isOk());
    librarian = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN).getFirst();
    assertTrue(librarian.isEnabled());
  }

  @Test
  void shouldThrow401WhenDisabledLibrarianUsedValidEndpoint() throws Exception {
    StaffAccount librarian = staffAccountRepository.findAllByRole(StaffAccountRole.LIBRARIAN).getFirst();
    librarian.setEnabled(false);
    staffAccountRepository.save(librarian);
    mockMvc.perform(patch("/api/books", librarian.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(httpBasic("librarian-test", "LibrarianPassword123!")))
            .andExpect(status().isUnauthorized());
  }
}
