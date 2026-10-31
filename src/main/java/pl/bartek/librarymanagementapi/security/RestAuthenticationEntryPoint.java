package pl.bartek.librarymanagementapi.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final SecurityProblemWriter securityProblemWriter;

  public RestAuthenticationEntryPoint(SecurityProblemWriter securityProblemWriter) {
    this.securityProblemWriter = securityProblemWriter;
  }

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException, ServletException {
    response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Library Management API\"");
    securityProblemWriter.write(response, HttpStatus.UNAUTHORIZED,
            "Authentication is required or credentials are invalid.");
  }
}
