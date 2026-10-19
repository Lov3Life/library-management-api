package pl.bartek.librarymanagementapi.readers;

import java.util.List;

public interface ReaderService {

  List<ReaderDTO> getReaders(String firstName, String lastName);

  ReaderDTO getReader(Integer id);

  ReaderDTO saveReader(ReaderCreateRequest readerCreateRequest);
}
