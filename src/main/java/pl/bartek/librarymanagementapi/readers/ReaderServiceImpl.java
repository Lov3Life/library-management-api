package pl.bartek.librarymanagementapi.readers;

import org.springframework.stereotype.Service;
import pl.bartek.librarymanagementapi.readers.exceptions.ReaderCreateInvalidParameterException;
import pl.bartek.librarymanagementapi.readers.exceptions.ReaderNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReaderServiceImpl implements ReaderService {

  private final ReaderRepository readerRepository;

  public ReaderServiceImpl(ReaderRepository readerRepository) {
    this.readerRepository = readerRepository;
  }

  @Override
  public List<ReaderDTO> getReaders(String firstName, String lastName) {
    List<Reader> readers;
    if(firstName != null) {
      firstName = normalize(firstName);
    }
    if(lastName != null) {
      lastName = normalize(lastName);
    }
    if(firstName != null && lastName != null){
      readers = readerRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
    } else if(firstName != null){
      readers = readerRepository.findByFirstNameContainingIgnoreCase(firstName);
    } else if(lastName != null){
      readers = readerRepository.findByLastNameContainingIgnoreCase(lastName);
    } else {
      readers = readerRepository.findAll();
    }
    return readers.stream()
            .map(ReaderDTO::fromReader)
            .toList();
  }

  @Override
  public ReaderDTO getReader(Integer id) {
    return readerRepository.findById(id)
            .map(ReaderDTO::fromReader)
            .orElseThrow(() -> new ReaderNotFoundException(id));
  }

  @Override
  public ReaderDTO saveReader(ReaderCreateRequest request) {
    Reader reader = ReaderCreateRequest.toReader(request);
    if(normalize(reader.getFirstName()) == null){
      throw new ReaderCreateInvalidParameterException("Reader first name cannot be empty");
    }
    if(normalize(reader.getLastName()) == null){
      throw new ReaderCreateInvalidParameterException("Reader last name cannot be empty");
    }
    if(reader.getBirthDate() == null || reader.getBirthDate().isAfter(LocalDate.now())){
      throw new ReaderCreateInvalidParameterException("Reader birth date cannot be empty or in the future");
    }
    reader.setFirstName(normalize(reader.getFirstName()));
    reader.setMiddleName(normalize(reader.getMiddleName()));
    reader.setLastName(normalize(reader.getLastName()));
    return ReaderDTO.fromReader(readerRepository.save(reader));
  }

  private String normalize(String input){
    if(input == null || input.isBlank()){
      return null;
    }
    return input.trim();
  }
}
