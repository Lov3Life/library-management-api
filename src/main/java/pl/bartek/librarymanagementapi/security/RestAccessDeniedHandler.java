package pl.bartek.librarymanagementapi.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

  private final SecurityProblemWriter securityProblemWriter;

  public RestAccessDeniedHandler(SecurityProblemWriter securityProblemWriter) {
    this.securityProblemWriter = securityProblemWriter;
  }

  @Override
  public void handle(HttpServletRequest request,
                     HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException, ServletException {
    securityProblemWriter.write(response, HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource.");
  }
}
