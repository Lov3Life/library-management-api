package pl.bartek.librarymanagementapi.readers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {

  private final ReaderService readerService;

  public ReaderController(ReaderService readerService) {
    this.readerService = readerService;
  }

  @GetMapping
  public List<ReaderDTO> getReaders(@RequestParam(required = false) String firstName,
                                    @RequestParam(required = false) String lastName) {
    return readerService.getReaders(firstName, lastName);
  }

  @GetMapping("/{id}")
  public ReaderDTO getReader(@PathVariable Integer id) {
    return readerService.getReader(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ReaderDTO saveReader(@RequestBody @Valid ReaderCreateRequest request) {
    return readerService.saveReader(request);
  }
}
