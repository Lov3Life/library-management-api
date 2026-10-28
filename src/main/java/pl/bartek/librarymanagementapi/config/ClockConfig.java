package pl.bartek.librarymanagementapi.config;

import org.springframework.boot.validation.autoconfigure.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

  @Bean
  public Clock clock(){
    return Clock.system(ZoneId.of("Europe/Warsaw"));
  }

  @Bean
  public ValidationConfigurationCustomizer validationClockCustomizer(Clock clock) {
    return configuration -> configuration.clockProvider(() -> clock);
  }
}
