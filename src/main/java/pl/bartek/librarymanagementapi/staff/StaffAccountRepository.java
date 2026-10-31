package pl.bartek.librarymanagementapi.staff;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffAccountRepository extends JpaRepository<StaffAccount, Integer> {

  Optional<StaffAccount> findByUsername(String username);
}
