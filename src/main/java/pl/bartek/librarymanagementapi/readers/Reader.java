package pl.bartek.librarymanagementapi.readers;

import jakarta.persistence.*;
import pl.bartek.librarymanagementapi.borrows.Borrow;
import pl.bartek.librarymanagementapi.logs.Log;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "readers")
public class Reader {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "middle_name", length = 100)
  private String middleName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @OneToMany(mappedBy = "reader")
  private Set<Borrow> borrows = new LinkedHashSet<>();

  @OneToMany(mappedBy = "reader")
  private Set<Log> logs = new LinkedHashSet<>();

  public Reader() {
  }

  public Reader(String firstName, String middleName, String lastName, LocalDate birthDate) {
    this.firstName = firstName;
    this.middleName = middleName;
    this.lastName = lastName;
    this.birthDate = birthDate;
  }

  public Integer getId() {
    return id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getMiddleName() {
    return middleName;
  }

  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }

  public boolean getActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Set<Borrow> getBorrows() {
    return borrows;
  }

  public void setBorrows(Set<Borrow> borrows) {
    this.borrows = borrows;
  }

  public Set<Log> getLogs() {
    return logs;
  }

  public void setLogs(Set<Log> logs) {
    this.logs = logs;
  }

  @Override
  public String toString() {
    return "Reader{" +
            "id=" + id +
            ", firstName='" + firstName + '\'' +
            ", middleName='" + middleName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", birthDate=" + birthDate +
            ", active=" + active +
            '}';
  }
}