package pl.bartek.librarymanagementapi.staff;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevAdminBootstrap {

  @Bean
  CommandLineRunner createAdmin(StaffAccountRepository repository,
                                PasswordEncoder passwordEncoder,
                                @Value("${ADMIN_USERNAME}") String username,
                                @Value("${ADMIN_PASSWORD}") String password) {
    return args -> {
      if(username.isBlank() || password.length() < 12)
        throw new IllegalArgumentException("Invalid bootstrap admin credentials.");
      if(repository.findByUsername(username).isEmpty()){
        StaffAccount admin = new StaffAccount(
                username,
                passwordEncoder.encode(password),
                StaffAccountRole.ADMIN);
        repository.save(admin);
      }
    };
  }
}
