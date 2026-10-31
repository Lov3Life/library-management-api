package pl.bartek.librarymanagementapi.staff;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "staff_accounts")
public class StaffAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "username", nullable = false, length = 100, unique = true)
  private String username;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  @Column(name = "role", nullable = false, length = 50)
  private StaffAccountRole role;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  protected StaffAccount() {
  }

  public StaffAccount(String username, String passwordHash, StaffAccountRole role) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
  }

  public Integer getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public StaffAccountRole getRole() {
    return role;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
