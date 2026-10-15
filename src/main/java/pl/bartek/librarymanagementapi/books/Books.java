package pl.bartek.librarymanagementapi.books;

import jakarta.persistence.*;
import pl.bartek.librarymanagementapi.borrows.Borrows;
import pl.bartek.librarymanagementapi.logs.Logs;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "books")
public class Books {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "author")
  private String author;

  @Column(name = "release_year")
  private Integer releaseYear;

  @Column(name = "publishing_house")
  private String publishingHouse;

  @Column(name = "active", nullable = false)
  private boolean active = true;

  @OneToMany(mappedBy = "book")
  private Set<Borrows> borrows = new LinkedHashSet<>();

  @OneToMany(mappedBy = "book")
  private Set<Logs> logs = new LinkedHashSet<>();

  public Books() {
  }

  public Books(String title, String author, Integer releaseYear, String publishingHouse) {
    this.title = title;
    this.author = author;
    this.releaseYear = releaseYear;
    this.publishingHouse = publishingHouse;
  }

  public Integer getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Integer getReleaseYear() {
    return releaseYear;
  }

  public void setReleaseYear(Integer releaseYear) {
    this.releaseYear = releaseYear;
  }

  public String getPublishingHouse() {
    return publishingHouse;
  }

  public void setPublishingHouse(String publishingHouse) {
    this.publishingHouse = publishingHouse;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Set<Borrows> getBorrows() {
    return borrows;
  }

  public void setBorrows(Set<Borrows> borrows) {
    this.borrows = borrows;
  }

  public Set<Logs> getLogs() {
    return logs;
  }

  public void setLogs(Set<Logs> logs) {
    this.logs = logs;
  }

  @Override
  public String toString() {
    return "Books{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", author='" + author + '\'' +
            ", releaseYear=" + releaseYear +
            ", publishingHouse='" + publishingHouse + '\'' +
            ", active=" + active +
            '}';
  }
}
