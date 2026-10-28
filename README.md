# Library Management API

A REST API built with **Java 21** and **Spring Boot 4.1.1** to manage physical book copies, library readers, borrowing records, and an audit history of important business events.

The application supports partial updates, validation, activation/deactivation rules, borrowing and returns, and paginated audit-log retrieval.

## Project status

| Module | Status |
|---|---|
| Books — management, validation, and tests | Implemented |
| Readers — management, validation, and tests | Implemented |
| Borrows — borrowing, returns, history, and tests | Implemented |
| Logs — business-event audit history and paginated read API | Implemented (dedicated tests pending) |

**Tech stack:** Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Data JPA / Hibernate, MySQL, Maven, JUnit 5, Mockito, and MockMvc. Request validation uses Jakarta Bean Validation; partial updates use Jackson's `JsonNullable`.

## Getting started

**Requirements:** JDK 21 and a running MySQL server on `localhost:3306`.

1. Create a MySQL database named `library_management`.
2. For a **fresh database**, execute [`database/db_DDL_schema.sql`](database/db_DDL_schema.sql) to create the tables and constraints.
3. Set the `DBUSER` and `DBPASSWORD` environment variables in your shell or IDE run configuration.
4. Start the application:

   **Windows (PowerShell):**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   **Linux / macOS:**

   ```bash
   ./mvnw spring-boot:run
   ```

The API is available at `http://localhost:8080`; `GET /` returns `OK`.

Hibernate uses `spring.jpa.hibernate.ddl-auto=validate`, which **validates the database schema but does not create or migrate tables**. If you already have an older database, apply the required schema changes before starting the updated application (see [Database](#database)). Do not run the fresh-install DDL blindly against existing tables.

## API

Successful retrievals and updates return `200 OK`; creation returns `201 Created`. Books and readers have database-generated IDs and separate endpoints for changing their `active` status. Ordinary book, reader, and borrow collection endpoints return JSON arrays; log collection endpoints return paginated responses.

**Books and Readers PATCH behavior:** Omitted JSON properties remain unchanged. Explicit `null` is allowed only for optional fields. An empty `{}` request is accepted and leaves the resource unchanged. Updates that produce no actual field changes do not create an audit event. Changing `active` uses dedicated endpoints.

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

The `title` query parameter performs a **case-insensitive substring search**. Leading and trailing whitespace is ignored; a blank value applies no title filter. The optional `active` parameter accepts `true` or `false`. Without it, both active and inactive books are returned.

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

This clears the author and release year without changing other fields. `author`, `releaseYear`, and `publishingHouse` can be cleared with `null`; `title` cannot be null, empty, or blank. A successful POST or PATCH returns the book representation, including its `id` and `active` status.

A book **cannot be deactivated while it has an open borrowing record** (`returnDate == null`). Attempting to do so returns `409 Conflict`. An inactive book cannot be borrowed.

### Readers

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/readers` | List readers; optionally filter by `firstName` and `lastName` |
| `GET` | `/api/readers/{id}` | Retrieve a reader |
| `POST` | `/api/readers` | Register a reader |
| `PATCH` | `/api/readers/{id}` | Update selected fields |
| `PATCH` | `/api/readers/{id}/activate` | Activate a reader |
| `PATCH` | `/api/readers/{id}/deactivate` | Deactivate a reader |

Reader filters use **case-insensitive substring matching**. Both parameters are optional and trimmed; when provided together, both must match. Empty or missing filters do not restrict the results. Reader listings do not expose an `active` query filter.

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

`firstName`, `lastName`, and `birthDate` are required; `middleName` is optional. Each name field accepts at most **100 characters**. Required names cannot be blank, and the birth date must be today or earlier (`yyyy-MM-dd`). Names are trimmed without changing their casing. Blank middle names become `null`, and new readers are active by default.

**Partial update — `PATCH /api/readers/1`:**

```json
{
  "middleName": null,
  "lastName": "O'Connor"
}
```

This clears the optional middle name and changes the surname. `firstName` and `lastName` cannot be null, empty, or blank; `birthDate` cannot be null or in the future. Successful creation and updates return the complete reader representation:

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

A reader **cannot be deactivated while they have an open borrowing record**. Attempting to do so returns `409 Conflict`. Inactive readers cannot borrow books.

### Borrows

Each borrowing record references a physical book copy and a reader. A record is open while `returnDate` is `null`; returning the book sets its return date and preserves the borrowing history.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/borrows` | List borrowing records; optionally filter by `readerId`, `bookId`, and `returned` |
| `GET` | `/api/borrows/{id}` | Retrieve a borrowing record |
| `POST` | `/api/borrows` | Borrow a copy for a reader |
| `PATCH` | `/api/borrows/{id}/return` | Return a borrowed copy (no request body) |

**Filtering** combines supplied parameters with AND. `returned=true` matches returned records, `returned=false` matches open records, and an omitted `returned` includes both. An empty result returns `[]`.

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

Both IDs are required. The book and reader must exist and be active, and the book copy must not already have an open borrow. The server provides `borrowDate` and returns `201 Created` with a `BorrowDTO`, for example:

```json
{
  "id": 18,
  "bookId": 5,
  "readerId": 12,
  "borrowDate": "2026-10-27",
  "returnDate": null
}
```

**Return — `PATCH /api/borrows/18/return`:** Sets `returnDate` to the current date, keeps the record, and returns the updated DTO (`200 OK`). Returning an already returned record causes `409 Conflict`. Attempting to borrow an already borrowed copy or to borrow using an inactive book or reader also causes `409 Conflict`. Missing book, reader, or borrow IDs cause `404 Not Found`.

The database's `uq_open_borrow` constraint prevents two simultaneous open borrowing records for the same physical copy.

### Logs — audit history

The Logs module automatically records **important business events** performed through the Books, Readers, and Borrows services. These are persisted audit records, **not** the application's console/SLF4J diagnostic logs.

Recorded event types:

| Event type | Trigger |
|---|---|
| `BOOK_CREATED` | A book copy is created |
| `BOOK_UPDATED` | A book's editable details change |
| `BOOK_ACTIVATED` | An inactive book is activated |
| `BOOK_DEACTIVATED` | An active book is deactivated |
| `READER_CREATED` | A reader is registered |
| `READER_UPDATED` | A reader's editable details change |
| `READER_ACTIVATED` | An inactive reader is activated |
| `READER_DEACTIVATED` | An active reader is deactivated |
| `BOOK_BORROWED` | A reader borrows a book copy |
| `BOOK_RETURNED` | A borrowed copy is returned |

**Audit behavior:**

- Each record stores a timestamp, event type, human-readable description, and applicable `readerId` and/or `bookId` references.
- Update descriptions include previous and new values for changed fields. No-op PATCH requests do not create `BOOK_UPDATED` or `READER_UPDATED` records.
- Audit records are written with the related business operation in a transaction, preventing a successful change from being committed without its log when an audit write fails.
- The configured `Clock` uses the `Europe/Warsaw` zone for application timestamps and is also supplied to Bean Validation. The API returns `LocalDateTime` values **without a UTC offset**.
- The API exposes log retrieval only; there are no public endpoints to manually create, modify, or delete audit records.

**Read endpoints:**

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/logs` | Retrieve all audit events |
| `GET` | `/api/logs/readers/{id}` | Retrieve audit events associated with a reader |
| `GET` | `/api/logs/books/{id}` | Retrieve audit events associated with a book |

Every endpoint supports pagination:

| Parameter | Default | Valid range | Meaning |
|---|---|---|---|
| `page` | `0` | `0` or greater | Zero-based page index |
| `limit` | `30` | `5`–`100` | Maximum items per page |

Results are sorted by **`date DESC`, then `id DESC`**, so the newest events appear first, with a consistent tie-breaker for events sharing the same timestamp. Invalid pagination parameters are rejected with `400 Bad Request`.

**Examples:**

```http
GET /api/logs?page=0&limit=30
GET /api/logs/readers/12?page=0&limit=10
GET /api/logs/books/5?page=1&limit=20
```

The response is a Spring Data `PagedModel`, containing a list of `LogDTO` objects and page metadata. An illustrative response (IDs, dates, and text are examples):

```json
{
  "content": [
    {
      "id": 42,
      "date": "2026-10-28T17:30:00",
      "eventType": "BOOK_BORROWED",
      "description": "Book The Hobbit borrowed by reader John Smith",
      "readerId": 12,
      "bookId": 5
    }
  ],
  "page": {
    "size": 30,
    "number": 0,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

For book-only events, `readerId` is `null`; for reader-only events, `bookId` is `null`. Borrow and return events reference both. A page with no matches returns an empty `content` array with pagination metadata, rather than a plain `[]` response.

### Responses and errors

The API uses Spring's `ProblemDetail` for handled errors. Activation and deactivation return the updated resource; requesting a status already set produces `409 Conflict`.

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid JSON, request data, or log pagination parameters |
| `404 Not Found` | The requested book, reader, or borrowing record does not exist |
| `409 Conflict` | Already active/inactive resource, open borrows blocking deactivation, already returned borrow, already borrowed copy, or inactive resource during borrowing |
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
| `logs` | Audit events with timestamps, types, descriptions, and optional foreign keys to books/readers |

A borrow remains open while `return_date` is `NULL`. The `borrows` table has a generated `open_book_id` and a uniqueness constraint to prevent concurrent open borrows for the same copy, plus a check preventing returns dated before borrowing.

The `logs` table includes:

- `id` — auto-increment primary key.
- `date` — non-null `DATETIME` (default `CURRENT_TIMESTAMP`).
- `event_type` — non-null `VARCHAR(50)` containing a `LogEventType` name.
- `description` — non-null `TEXT` message.
- `reader_id` and `book_id` — nullable foreign keys referencing the related entities.

**Upgrading an existing database:** Earlier versions of the `logs` table had no `event_type` column and used `VARCHAR(255)` for `description`. Update that schema before starting this version. If historical log rows already exist, determine how to backfill `event_type` before enforcing `NOT NULL`. The repository provides a fresh-install schema, not an automated migration tool.

## Tests

| Test class | Coverage |
|---|---|
| `BookServiceTest`, `ReaderServiceTest`, `BorrowServiceTest` | JUnit 5 / Mockito service-level tests for business rules and DTO mapping |
| `BookControllerTest`, `ReaderControllerTest`, `BorrowControllerTest` | MockMvc tests for routing, parameters, JSON, validation, and HTTP responses |
| `LibraryManagementApiApplicationTests` | `@SpringBootTest` application-context startup test |

There are **no dedicated Logs module tests yet**. The existing service and controller tests cover the other modules, including business rules preventing deactivation while open borrows exist.

Run the tests from the project root:

```powershell
.\mvnw.cmd test
```

On Linux or macOS, use `./mvnw test`. Unit and MVC tests mock dependencies and do not require a running database; the full suite includes `contextLoads()`, which requires MySQL, the initialized schema, and the `DBUSER` and `DBPASSWORD` environment variables to be available to Maven.

## Next steps

- Add unit tests for `LogDescriptionBuilder` and `LogServiceImpl` (including optional/null fields and unchanged updates).
- Add MockMvc tests for log pagination, filtering, and invalid query parameters.
- Consider database migrations (for example, Flyway) to manage schema changes automatically.
