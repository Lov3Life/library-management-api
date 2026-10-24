# Library Management API

A REST API built with **Java 21** and **Spring Boot** to manage physical book copies, library readers, and their borrowing history. The Books and Readers modules are implemented; borrowing workflows and audit logging are next.

## Project status

| Module | Status |
|---|---|
| Books — management, validation, and tests | Implemented |
| Readers — management, validation, and tests | Implemented |
| Borrows — borrowing and returning | Planned |
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

All successful list and retrieval requests return `200 OK`; creation returns `201 Created`, and updates return `200 OK`. Both modules use database-generated IDs and dedicated routes to change the `active` flag. An empty list returns `[]`.

**PATCH behavior:** Omitted JSON properties remain unchanged; explicitly provided `null` values are handled according to the field's constraints. An empty `{}` update is accepted without changing the resource. `active` is not modified through the general PATCH endpoint.

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

### Responses and errors

The API uses Spring's `ProblemDetail` for handled errors. Activation and deactivation return the updated resource; requesting an already-set status produces `409 Conflict`.

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid JSON or request data, including failed field validation |
| `404 Not Found` | The requested book or reader does not exist |
| `409 Conflict` | The book or reader is already in the requested active/inactive state |
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

A borrow remains open while `return_date` is `NULL`. The schema has a generated `open_book_id` with a uniqueness constraint to prevent concurrent open records for the same physical copy, plus a check that a return date cannot precede the borrow date. The `borrows` and `logs` entities and tables are in place, but their API workflows are not implemented yet.

## Tests

| Test class | Coverage |
|---|---|
| `BookServiceTest` and `ReaderServiceTest` | JUnit 5 and Mockito unit tests for service logic |
| `BookControllerTest` and `ReaderControllerTest` | MockMvc tests for routing, JSON, validation, and HTTP responses |
| `LibraryManagementApiApplicationTests` | `@SpringBootTest` application-context startup test |

Run all tests from the project root:

```powershell
.\mvnw.cmd test
```

On Linux or macOS, use `./mvnw test`. Service and MVC tests mock their dependencies and do not require a running database; **the full suite includes `contextLoads()`**, which requires MySQL, the initialized schema, and environment variables `DBUSER` and `DBPASSWORD` to be available to Maven.

## Next steps

Implement the **Borrows API** (checkout, return, availability checks, and borrowing history), followed by the **Logs API** for audit events. The database mappings exist, but the corresponding endpoints and business logic are not yet available.
