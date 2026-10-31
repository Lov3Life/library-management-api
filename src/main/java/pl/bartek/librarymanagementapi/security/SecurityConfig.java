package pl.bartek.librarymanagementapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http,
                                          RestAuthenticationEntryPoint authenticationEntryPoint,
                                          RestAccessDeniedHandler accessDeniedHandler) {
    return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/error").permitAll()
                    .requestMatchers(
                            "/api/logs", "/api/logs/**",
                            "/api/staff", "/api/staff/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                            "/api/books", "/api/books/**",
                            "/api/readers", "/api/readers/**",
                            "/api/borrows", "/api/borrows/**")
                    .hasAnyRole("LIBRARIAN", "ADMIN")
                    .anyRequest().denyAll())
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
            .httpBasic(basic -> basic
                    .authenticationEntryPoint(authenticationEntryPoint))
            .formLogin(AbstractHttpConfigurer::disable)
            .build();
  }
}
