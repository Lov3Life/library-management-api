# Library Management API

A REST API built with **Java 21** and **Spring Boot** to manage physical book copies, library readers, and their borrowing history. The Books, Readers, and Borrows modules are implemented and covered by service and controller tests. Audit logging is planned.

## Project status

| Module | Status |
|---|---|
| Books — management, validation, and tests | Implemented |
| Readers — management, validation, and tests | Implemented |
| Borrows — borrowing, returns, history, and tests | Implemented |
| Logs — audit history | Planned |

**Stack:** Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA / Hibernate, MySQL, Maven, JUnit 5, Mockito, and MockMvc. Request validation uses Jakarta Bean Validation; partial updates use Jackson's `JsonNullable`.

## Getting started

**Requirements:** JDK 21 and a running MySQL server on `localhost:3306`.

1. Create a MySQL database named `library_management`.
2. Run [`database/db_DDL_schema.sql`](database/db_DDL_schema.sql) to create the tables and constraints.
3. Set `DBUSER` and `DBPASSWORD` in your environment or IDE run configuration.
4. Start the application:

   **Windows (PowerShell):**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   **Linux / macOS:**
   ```bash
   ./mvnw spring-boot:run
   ```

The API is available at `http://localhost:8080`; `GET /` returns `OK`. Hibernate uses `spring.jpa.hibernate.ddl-auto=validate`, so **it checks the existing schema rather than creating tables**.

## API

All successful list and retrieval requests return `200 OK`; creation returns `201 Created`, and updates return `200 OK`. Books and Readers use database-generated IDs and dedicated routes to change the `active` flag. An empty list returns `[]`.

**Books and Readers PATCH behavior:** Omitted JSON properties remain unchanged; explicitly provided `null` values are handled according to field constraints. An empty `{}` update is accepted without changing the resource. Changing `active` uses dedicated endpoints.

### Books

One row in `books` represents **one physical copy**, not a book title shared by several copies.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/books` | List books; optionally filter by `title` and `active` |
| `GET` | `/api/books/{id}` | Retrieve a book |
| `POST` | `/api/books` | Create a book |
| `PATCH` | `/api/books/{id}` | Update selected fields |
| `PATCH` | `/api/books/{id}/activate` | Activate a book |
| `PATCH` | `/api/books/{id}/deactivate` | Deactivate a book |

The `title` query parameter performs a **case-insensitive substring search**; leading and trailing whitespace is ignored, and a blank value applies no title filter. The `active` parameter accepts `true` or `false`. Without it, both active and inactive books are returned.

```http
GET /api/books?title=Hobbit&active=true
```

**Create — `POST /api/books`:**

```json
{
  "title": "The Hobbit",
  "author": "J.R.R. Tolkien",
  "releaseYear": 1937,
  "publishingHouse": "George Allen & Unwin"
}
```

Only `title` is required, and it cannot be blank. `author`, `releaseYear`, and `publishingHouse` are optional. Text fields are trimmed; blank optional text is stored as `null`. New books are active by default.

**Partial update — `PATCH /api/books/1`:**

```json
{
  "author": null,
  "releaseYear": null
}
```

This clears the author and release year without changing the other properties. `author`, `releaseYear`, and `publishingHouse` may be cleared with `null`; `title` cannot be null, empty, or blank. A successful POST or PATCH returns the book representation, including its `id` and `active` status.

### Readers

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/readers` | List readers; optionally filter by `firstName` and `lastName` |
| `GET` | `/api/readers/{id}` | Retrieve a reader |
| `POST` | `/api/readers` | Register a reader |
| `PATCH` | `/api/readers/{id}` | Update selected fields |
| `PATCH` | `/api/readers/{id}/activate` | Activate a reader |
| `PATCH` | `/api/readers/{id}/deactivate` | Deactivate a reader |

Reader filters use **case-insensitive substring matching**. Both parameters are optional and trimmed; when supplied together, both must match. Empty or missing filters do not restrict the results. Reader listings have no `active` query filter.

```http
GET /api/readers?firstName=John&lastName=Smith
```

**Create — `POST /api/readers`:**

```json
{
  "firstName": "John",
  "middleName": null,
  "lastName": "Smith",
  "birthDate": "1998-05-21"
}
```

`firstName`, `lastName`, and `birthDate` are required; `middleName` is optional. Each name field accepts at most **100 characters**. Required names cannot be blank, and the birth date must be today or earlier (`yyyy-MM-dd`). Names are trimmed without changing their casing, preserving forms such as `McDonald`, `O'Connor`, and `Kowalska-Nowak`. Blank middle names become `null`, and new readers are active by default.

**Partial update — `PATCH /api/readers/1`:**

```json
{
  "middleName": null,
  "lastName": "O'Connor"
}
```

This clears the optional middle name and changes the surname. `firstName` and `lastName` cannot be null, empty, or blank; `birthDate` cannot be null or in the future. The same 100-character limits and casing-preservation rules apply to PATCH. Successful creation and updates return the full reader representation, for example:

```json
{
  "id": 1,
  "firstName": "John",
  "middleName": null,
  "lastName": "O'Connor",
  "birthDate": "1998-05-21",
  "active": true
}
```

### Borrows

Each borrowing record references one physical book copy and one reader. A record is open while `returnDate` is `null`; returning a book sets the return date and preserves the borrowing history.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/borrows` | List borrowing records, optionally filtered by `readerId`, `bookId`, and `returned` |
| `GET` | `/api/borrows/{id}` | Retrieve a borrowing record |
| `POST` | `/api/borrows` | Borrow a book copy for a reader |
| `PATCH` | `/api/borrows/{id}` | Return a previously borrowed copy (no request body) |

**Filtering** combines supplied parameters with AND. `returned=true` matches records with a return date, `returned=false` matches open records, and an omitted `returned` includes both. Filters may be combined or omitted entirely. An empty result returns `[]`.

```http
GET /api/borrows?readerId=12&bookId=5&returned=false
```

**Create — `POST /api/borrows`:**

```json
{
  "bookId": 5,
  "readerId": 12
}
```

Both IDs are required. The book and reader must exist and be active, and the physical copy must not already have an open borrowing record. The server supplies `borrowDate` and returns `201 Created` with a `BorrowDTO`, for example:

```json
{
  "id": 18,
  "bookId": 5,
  "readerId": 12,
  "borrowDate": "2026-10-27",
  "returnDate": null
}
```

**Return — `PATCH /api/borrows/18`:** Sets `returnDate` to the current date, preserves the record, and returns the updated DTO (`200 OK`). Returning an already returned record causes `409 Conflict`. Borrowing a copy that is already borrowed, or borrowing for an inactive book or reader, also causes `409 Conflict`. Missing book, reader, or borrowing IDs cause `404 Not Found`.

The database's `uq_open_borrow` constraint prevents two simultaneous open borrowing records for the same copy.

### Responses and errors

The API uses Spring's `ProblemDetail` for handled errors. Activation and deactivation return the updated resource; requesting an already-set status produces `409 Conflict`.

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid JSON or request data, including failed field validation |
| `404 Not Found` | The requested book, reader, or borrowing record does not exist |
| `409 Conflict` | Repeated status change, already returned borrow, already borrowed copy, or inactive resource during borrowing |
| `500 Internal Server Error` | Unexpected server error |

Example `404` response (selected properties):

```json
{
  "status": 404,
  "detail": "Book with id: 999 not found"
}
```

## Database

The schema is defined in [`database/db_DDL_schema.sql`](database/db_DDL_schema.sql).

| Table | Purpose |
|---|---|
| `books` | Physical book copies and their active status |
| `readers` | Reader profiles and their active status |
| `borrows` | Borrowing records and return dates |
| `logs` | Audit records referencing books and/or readers |

A borrow remains open while `return_date` is `NULL`. The schema has a generated `open_book_id` with a uniqueness constraint to prevent concurrent open records for the same physical copy, plus a check that a return date cannot precede the borrow date. The Borrows API is implemented and tested; the Logs API is not yet implemented.

## Tests

| Test class | Coverage |
|---|---|
| `BookServiceTest`, `ReaderServiceTest`, and `BorrowServiceTest` | JUnit 5 and Mockito unit tests for service logic, business rules, and DTO mapping |
| `BookControllerTest`, `ReaderControllerTest`, and `BorrowControllerTest` | MockMvc tests for routing, query parameters, JSON, validation, and HTTP responses |
| `LibraryManagementApiApplicationTests` | `@SpringBootTest` application-context startup test |

Run all tests from the project root:

```powershell
.\mvnw.cmd test
```

On Linux or macOS, use `./mvnw test`. Service and MVC tests mock their dependencies and do not require a running database; **the full suite includes `contextLoads()`**, which requires MySQL, the initialized schema, and environment variables `DBUSER` and `DBPASSWORD` to be available to Maven.

## Next steps

Implement the **Logs API** for audit events.
