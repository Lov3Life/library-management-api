package pl.bartek.librarymanagementapi.readers;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.bartek.librarymanagementapi.borrows.BorrowRepository;
import pl.bartek.librarymanagementapi.logs.LogEventType;
import pl.bartek.librarymanagementapi.logs.LogService;
import pl.bartek.librarymanagementapi.readers.exceptions.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReaderServiceImpl implements ReaderService {

  private final ReaderRepository readerRepository;
  private final BorrowRepository borrowRepository;
  private final LogService logService;
  private final Clock clock;

  public ReaderServiceImpl(ReaderRepository readerRepository, BorrowRepository borrowRepository, LogService logService, Clock clock) {
    this.readerRepository = readerRepository;
    this.borrowRepository = borrowRepository;
    this.logService = logService;
    this.clock = clock;
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
  @Transactional
  public ReaderDTO saveReader(ReaderCreateRequest request) {
    Reader reader = ReaderCreateRequest.toReader(request);
    if(normalize(reader.getFirstName()) == null){
      throw new ReaderCreateInvalidParameterException("Reader first name cannot be empty.");
    }
    if(normalize(reader.getLastName()) == null){
      throw new ReaderCreateInvalidParameterException("Reader last name cannot be empty.");
    }
    if(reader.getBirthDate() == null || reader.getBirthDate().isAfter(LocalDate.now(clock))){
      throw new ReaderCreateInvalidParameterException("Reader birth date cannot be empty or in the future.");
    }
    reader.setFirstName(normalize(reader.getFirstName()));
    reader.setMiddleName(normalize(reader.getMiddleName()));
    reader.setLastName(normalize(reader.getLastName()));
    Reader savedReader = readerRepository.save(reader);
    logService.logEvent(LogEventType.READER_CREATED, savedReader, null);
    return ReaderDTO.fromReader(savedReader);
  }

  @Override
  @Transactional
  public ReaderDTO updateReader(Integer id, ReaderUpdateRequest request) {
    Reader reader = readerRepository.findById(id)
            .orElseThrow(() -> new ReaderNotFoundException(id));
    ReaderDTO previous = ReaderDTO.fromReader(reader);
    if(request.firstName().isPresent()){
      if(normalize(request.firstName().get()) == null){
        throw new ReaderUpdateInvalidParameterException(
                "Reader first name cannot be blank or Null. If change is no needed, don't fill param.");
      }
      reader.setFirstName(normalize(request.firstName().get()));
    }
    if(request.middleName().isPresent()){
      reader.setMiddleName(normalize(request.middleName().get()));
    }
    if(request.lastName().isPresent()){
      if(normalize(request.lastName().get()) == null){
        throw new ReaderUpdateInvalidParameterException(
                "Reader last name cannot be blank or Null. If change is no needed, don't fill param.");
      }
      reader.setLastName(normalize(request.lastName().get()));
    }
    if(request.birthDate().isPresent()){
      LocalDate birthDate = request.birthDate().get();
      if(birthDate == null || birthDate.isAfter(LocalDate.now(clock))){
        throw new ReaderUpdateInvalidParameterException(
                "Reader birth date cannot be blank, Null, or in the future. If change is no needed, don't fill param.");
      }
      reader.setBirthDate(birthDate);
    }
    ReaderDTO current = ReaderDTO.fromReader(reader);
    if(previous.equals(current)){
      return current;
    }else {
      logService.logEvent(LogEventType.READER_UPDATED, reader, null, previous, null);
      return ReaderDTO.fromReader(readerRepository.save(reader));
    }
  }

  @Override
  @Transactional
  public ReaderDTO deactivateReader(Integer id) {
    Reader reader = readerRepository.findById(id)
            .orElseThrow(() -> new ReaderNotFoundException(id));
    if(!reader.isActive()){
      throw new ReaderAlreadyInactiveException(id);
    }
    if(borrowRepository.existsByReaderIdAndReturnDateIsNull(id)){
      throw new ReaderHasActiveBorrowsException(id);
    }
    reader.setActive(false);
    logService.logEvent(LogEventType.READER_DEACTIVATED, reader, null);
    return ReaderDTO.fromReader(readerRepository.save(reader));
  }

  @Override
  @Transactional
  public ReaderDTO activateReader(Integer id) {
    Reader reader = readerRepository.findById(id)
            .orElseThrow(() -> new ReaderNotFoundException(id));
    if(reader.isActive()){
      throw new ReaderAlreadyActiveException(id);
    }
    reader.setActive(true);
    logService.logEvent(LogEventType.READER_ACTIVATED, reader, null);
    return ReaderDTO.fromReader(readerRepository.save(reader));
  }

  private String normalize(String input){
    if(input == null || input.isBlank()){
      return null;
    }
    return input.trim();
  }
}
