package pl.bartek.librarymanagementapi.borrows;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {

  private final BorrowService borrowService;

  public BorrowController(BorrowService borrowService) {
    this.borrowService = borrowService;
  }

  @GetMapping
  public List<BorrowDTO> getBorrows(@RequestParam(required = false) Integer readerId,
                                    @RequestParam(required = false) Integer bookId,
                                    @RequestParam(required = false) Boolean returned) {
    return borrowService.getBorrows(readerId, bookId, returned);
  }

  @GetMapping("/{id}")
  public BorrowDTO getBorrow(@PathVariable Integer id) {
    return borrowService.getBorrow(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BorrowDTO createBorrow(@Valid @RequestBody BorrowCreateRequest request) {
    return borrowService.createBorrow(request);
  }

  @PatchMapping("/{id}/return")
  @ResponseStatus(HttpStatus.OK)
  public BorrowDTO returnBook(@PathVariable Integer id) {
    return borrowService.returnBook(id);
  }
}
