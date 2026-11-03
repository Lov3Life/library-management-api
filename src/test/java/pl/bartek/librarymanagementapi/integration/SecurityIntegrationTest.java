package pl.bartek.librarymanagementapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pl.bartek.librarymanagementapi.integration.config.MySqlTestConfiguration;
import pl.bartek.librarymanagementapi.staff.StaffAccount;
import pl.bartek.librarymanagementapi.staff.StaffAccountRepository;
import pl.bartek.librarymanagementapi.staff.StaffAccountRole;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
  void should2xxWhenRequestIsAnonymous(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint))
            .andExpect(status().is2xxSuccessful());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/books", "/api/readers", "/api/borrows", "/api/staff/librarians", "/api/logs", "/api/other"})
  void should4xxWhenRequestIsAnonymous(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint))
            .andExpect(status().is4xxClientError());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/books", "/api/readers", "/api/borrows"})
  void should2xxWhenRequestIsLibrarian(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("librarian-test", "LibrarianPassword123!")))
            .andExpect(status().is2xxSuccessful());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/staff/librarians", "/api/logs", "/api/other"})
  void should4xxWhenRequestIsLibrarian(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("librarian-test", "LibrarianPassword123!")))
            .andExpect(status().is4xxClientError());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/books", "/api/readers", "/api/borrows", "/api/staff/librarians", "/api/logs"})
  void should2xxWhenRequestIsAdmin(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("admin-test", "AdminPassword123!")))
            .andExpect(status().is2xxSuccessful());
  }

  @ParameterizedTest
  @ValueSource(strings = {"/api/other"})
  void should4xxWhenRequestIsAdmin(String endpoint) throws Exception {
    mockMvc.perform(get(endpoint)
                    .with(httpBasic("admin-test", "AdminPassword123!")))
            .andExpect(status().is4xxClientError());
  }

  @Test
  void should403WhenPasswordIsWrong() throws Exception {
    mockMvc.perform(get("/api/books")
                    .with(httpBasic("librarian-test", "WrongPassword")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("Authentication is required or credentials are invalid."));
  }

  @Test
  void should403WhenUsernameIsNotExist() throws Exception {
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
}
