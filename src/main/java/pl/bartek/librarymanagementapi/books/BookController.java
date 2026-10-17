package pl.bartek.librarymanagementapi.books;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService){
    this.bookService = bookService;
  }

  @GetMapping
  public List<BookDTO> getBooks(@RequestParam(required = false) String title,
                                @RequestParam(required = false) Boolean active){
    return bookService.getBooks(title, active);
  }

  @GetMapping("/{id}")
  public BookDTO getBook(@PathVariable Integer id){
    return bookService.getBook(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookDTO saveBook(@Valid @RequestBody BookCreateRequest request){
    return bookService.saveBook(request);
  }

  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public BookDTO updateBook(@PathVariable Integer id,
                            @Valid @RequestBody BookUpdateRequest request){
    return bookService.updateBook(id, request);
  }

  @PatchMapping("/{id}/activate")
  @ResponseStatus(HttpStatus.OK)
  public BookDTO activateBook(@PathVariable Integer id){
    return bookService.activateBook(id);
  }

  @PatchMapping("/{id}/deactivate")
  @ResponseStatus(HttpStatus.OK)
  public BookDTO deactivateBook(@PathVariable Integer id){
    return bookService.deactivateBook(id);
  }

}
