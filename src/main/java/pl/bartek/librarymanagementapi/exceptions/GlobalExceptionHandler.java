package pl.bartek.librarymanagementapi.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.bartek.librarymanagementapi.books.exceptions.*;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(BookNotFoundException.class)
  public ProblemDetail handleBookNotFound(BookNotFoundException ex){
    return ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(BookAlreadyActiveException.class)
  public ProblemDetail handleBookAlreadyActive(BookAlreadyActiveException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(BookAlreadyInactiveException.class)
  public ProblemDetail handleBookAlreadyDeactivate(BookAlreadyInactiveException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(BookCreateInvalidParameterException.class)
  public ProblemDetail handleBookCreateInvalidParameter(BookCreateInvalidParameterException ex){
    return ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(BookUpdateInvalidParameterException.class)
  public ProblemDetail handleBookUpdateInvalidParameter(BookUpdateInvalidParameterException ex){
    return ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex){
    LOGGER.error("Unexpected exception occurred", ex);
    return ProblemDetail.forStatusAndDetail(INTERNAL_SERVER_ERROR, "An unexpected server error occurred");
  }
}
