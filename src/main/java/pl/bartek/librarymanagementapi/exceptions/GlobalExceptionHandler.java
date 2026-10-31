package pl.bartek.librarymanagementapi.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pl.bartek.librarymanagementapi.books.exceptions.*;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowAlreadyReturnedException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowConflictException;
import pl.bartek.librarymanagementapi.borrows.exceptions.BorrowNotFoundException;
import pl.bartek.librarymanagementapi.readers.exceptions.*;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffConflictException;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffInvalidParameterException;
import pl.bartek.librarymanagementapi.staff.exceptions.StaffNotFoundException;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger exceptionLogger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

  @ExceptionHandler(BookHasActiveBorrowsException.class)
  public ProblemDetail handleBookHasActiveBorrowsException(BookHasActiveBorrowsException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(ReaderNotFoundException.class)
  public ProblemDetail handleReaderNotFoundException(ReaderNotFoundException ex){
    return ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(ReaderCreateInvalidParameterException.class)
  public ProblemDetail handleReaderCreateInvalidParameter(ReaderCreateInvalidParameterException ex){
    return ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ReaderUpdateInvalidParameterException.class)
  public ProblemDetail handleReaderUpdateInvalidParameter(ReaderUpdateInvalidParameterException ex){
    return ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ReaderAlreadyActiveException.class)
  public ProblemDetail handleReaderAlreadyActive(ReaderAlreadyActiveException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(ReaderAlreadyInactiveException.class)
  public ProblemDetail handleReaderAlreadyDeactivate(ReaderAlreadyInactiveException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(ReaderHasActiveBorrowsException.class)
  public ProblemDetail handleReaderHasActiveBorrows(ReaderHasActiveBorrowsException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(BorrowConflictException.class)
  public ProblemDetail handleBorrowCreateInvalidParameterException(BorrowConflictException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(BorrowNotFoundException.class)
  public ProblemDetail handleBorrowNotFoundException(BorrowNotFoundException ex){
    return ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(BorrowAlreadyReturnedException.class)
  public ProblemDetail handleBorrowReturnInvalidParameterException(BorrowAlreadyReturnedException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(StaffConflictException.class)
  public ProblemDetail handleStaffConflictException(StaffConflictException ex){
    return ProblemDetail.forStatusAndDetail(CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(StaffNotFoundException.class)
  public ProblemDetail handleStaffNotFoundException(StaffNotFoundException ex){
    return ProblemDetail.forStatusAndDetail(NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(StaffInvalidParameterException.class)
  public ProblemDetail handleStaffInvalidParameterException(StaffInvalidParameterException ex){
    return ProblemDetail.forStatusAndDetail(BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex){
    exceptionLogger.error("Unexpected exception occurred", ex);
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            INTERNAL_SERVER_ERROR, "An unexpected server error occurred");
    return ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(problemDetail);
  }
}
