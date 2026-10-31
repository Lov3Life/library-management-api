# Library Management API

A REST API built with **Java 21** and **Spring Boot 4.1.1** for managing physical book copies, library readers, borrowing records, staff accounts, and an audit history of business events.

The application provides validated CRUD-style operations, partial updates, activation/deactivation rules, borrowing and returns, paginated audit logs, and **Spring Security authentication and role-based authorization**.

## Project status

| Module | Status |
|---|---|
| Books — catalog management, validation, activation and deactivation | Implemented and tested |
| Readers — reader management, validation, activation and deactivation | Implemented and tested |
| Borrows — borrowing, returns, history and business rules | Implemented and tested |
| Logs — transactional business-event audit history and paginated read API | Implemented and tested |
| Staff — staff accounts, creation, activation and deactivation | Implemented; dedicated automated tests planned |
| Security — HTTP Basic, BCrypt, database-backed users and role-based access | Implemented; manually verified in Postman |
| Automated testing — service unit tests, MVC tests and MySQL integration tests | Implemented for existing business modules; security coverage planned |
| Application Docker image and Docker Compose deployment | Planned |

**Tech stack:** Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Security, Spring Data JPA / Hibernate, MySQL, Jakarta Bean Validation, Maven, and Jackson's `JsonNullable` for partial updates.

**Testing:** JUnit 5, Mockito, MockMvc, Spring Boot Test, Spring Security Test, Testcontainers and MySQL 8.4. Docker currently provisions the **test database**; there is no application `Dockerfile` or deployment `compose.yaml` yet.

## Getting started

### Requirements

- JDK 21.
- A running MySQL server (by default, `localhost:3306`).
- The Maven Wrapper included in the repository; a separate Maven installation is not required.

Docker is **not** needed to start the application locally with an existing MySQL server. It **is** needed for the integration test suite; see [Tests](#tests).

### 1. Prepare MySQL

Create a database named `library_management`. For a **new, empty database**, run [`database/db_DDL_schema.sql`](database/db_DDL_schema.sql). The script creates the `books`, `readers`, `borrows`, `logs`, and `staff_accounts` tables and their constraints.

> **Existing database:** The SQL file describes a fresh installation, not an incremental migration. If you have an older schema, apply the changes required for the current version (including `staff_accounts`) instead of rerunning the entire script.

The application uses `spring.jpa.hibernate.ddl-auto=validate`: Hibernate **validates** the schema but does not create or update tables for you.

### 2. Configure environment variables

The application reads its MySQL credentials from:

| Variable | Required | Purpose |
|---|---|---|
| `DBUSER` | Yes | MySQL username |
| `DBPASSWORD` | Yes | MySQL password |
| `SPRING_PROFILES_ACTIVE` | For first-time development bootstrap | Set to `dev` to enable creation of an initial admin account |
| `ADMIN_USERNAME` | With `dev` profile | Username for the initial admin |
| `ADMIN_PASSWORD` | With `dev` profile | Password for the initial admin (at least 12 characters) |

Set these variables in your shell or IntelliJ IDEA run configuration. **Do not commit real passwords, `.env` files containing secrets, or exported run configurations.**

### 3. Create the first administrator (development)

`DevAdminBootstrap` is active **only** under the `dev` Spring profile. On startup, it checks whether the configured username already exists; if not, it creates an enabled account with role `ADMIN` and stores a **BCrypt hash**, not the plaintext password.

For a new local database, set `SPRING_PROFILES_ACTIVE=dev`, `ADMIN_USERNAME` and `ADMIN_PASSWORD` before running the app. The bootstrap does not update an existing account or reset its password on subsequent starts.

**Important:** Without `dev` bootstrap (or a separately provisioned admin), an empty database has no administrator. The current API deliberately has **no public registration endpoint**. Production-safe initial account provisioning is planned for the deployment stage.

### 4. Start the API

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

Default base URL: `http://localhost:8080`.

The public endpoint `GET /` returns `200 OK` with the text `OK`. All existing book, reader, borrow, log, and staff endpoints require authentication.

### 5. Authenticate requests

The API uses **HTTP Basic authentication**. In Postman, select **Authorization → Basic Auth** and enter the credentials of an enabled staff account. There is no separate `/login` endpoint and no JWT token exchange.

For example, using curl (the password is prompted interactively):

```bash
curl --user admin http://localhost:8080/api/books
```

HTTP Basic credentials are sent with requests; use **HTTPS for any non-local deployment**. Plain HTTP on `localhost` is intended only for development.

## Authentication and authorization

Staff accounts are stored in MySQL (`staff_accounts`). Spring Security obtains them through a custom `UserDetailsService` and validates passwords using `BCryptPasswordEncoder`. Disabled accounts cannot authenticate. Requests are configured as stateless (`SessionCreationPolicy.STATELESS`).

There are two roles:

- `ADMIN` — can manage books, readers and borrows, read audit logs, and manage staff accounts.
- `LIBRARIAN` — can manage books, readers and borrows but cannot access audit logs or staff management.

The security configuration uses `ROLE_ADMIN` and `ROLE_LIBRARIAN` authorities. Unknown endpoint paths are denied by default.

| Resource | Anonymous | `LIBRARIAN` | `ADMIN` |
|---|:---:|:---:|:---:|
| `GET /` | Allowed | Allowed | Allowed |
| `/api/books` and `/api/books/**` | Denied | Allowed | Allowed |
| `/api/readers` and `/api/readers/**` | Denied | Allowed | Allowed |
| `/api/borrows` and `/api/borrows/**` | Denied | Allowed | Allowed |
| `/api/logs` and `/api/logs/**` | Denied | Denied | Allowed |
| `/api/staff` and `/api/staff/**` | Denied | Denied | Allowed |

For protected endpoints:

- `401 Unauthorized` — credentials are missing, incorrect, or the account is disabled.
- `403 Forbidden` — the authenticated account lacks the required role.

Custom `AuthenticationEntryPoint` and `AccessDeniedHandler` implementations return Spring `ProblemDetail` responses with `Content-Type: application/problem+json`. HTTP Basic `401` responses include a `WWW-Authenticate` header.

**Current security scope:** CSRF protection is disabled for the present stateless, non-browser API usage. Reassess CSRF if the API is later used with browser-managed credentials or cookies. Rate limiting, more granular permissions and automated authentication/authorization tests are not implemented yet. Audit entries do **not** currently identify which authenticated staff member performed an action.

## API

Unless specified otherwise, successful retrievals and updates return `200 OK`; creation returns `201 Created`. Books and readers have database-generated IDs and dedicated endpoints for changing their `active` status. Book, reader and borrow collection endpoints return JSON arrays; log collection endpoints return paginated objects.

**Books and Readers PATCH behavior:** Omitted JSON fields are left unchanged. Explicit `null` is accepted only for optional fields. An empty `{}` leaves the resource unchanged. Updates producing no effective changes do not create an audit event. Changing the `active` flag requires a dedicated endpoint.

### Books

One row in `books` represents **one physical copy** of a book, not a shared title with a quantity.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/books` | List books; optionally filter by `title` and `active` |
| `GET` | `/api/books/{id}` | Retrieve a book |
| `POST` | `/api/books` | Create a book |
| `PATCH` | `/api/books/{id}` | Update selected fields |
| `PATCH` | `/api/books/{id}/activate` | Activate a book |
| `PATCH` | `/api/books/{id}/deactivate` | Deactivate a book |

The `title` filter performs a **case-insensitive substring search**. Leading/trailing whitespace is ignored; a blank value applies no title filter. `active` can be `true` or `false`; when omitted, both active and inactive books are returned.

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

Only `title` is required and it cannot be blank. `author`, `releaseYear` and `publishingHouse` are optional. Text fields are trimmed; blank optional text becomes `null`. Books are active by default.

**Partial update — `PATCH /api/books/1`:**

```json
{
  "author": null,
  "releaseYear": null
}
```

This clears the author and release year without changing other fields. `title` cannot be cleared. Successful create/update calls return the full `BookDTO`, including `id` and `active`.

A book cannot be deactivated if it has an **open borrowing record** (`returnDate == null`); the request returns `409 Conflict`. Inactive books cannot be borrowed.

### Readers

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/readers` | List readers; optionally filter by `firstName` and `lastName` |
| `GET` | `/api/readers/{id}` | Retrieve a reader |
| `POST` | `/api/readers` | Register a reader |
| `PATCH` | `/api/readers/{id}` | Update selected fields |
| `PATCH` | `/api/readers/{id}/activate` | Activate a reader |
| `PATCH` | `/api/readers/{id}/deactivate` | Deactivate a reader |

Name filters are **case-insensitive substring matches**, trimmed and combined with AND when both are provided. An omitted or blank filter does not restrict results. The endpoint does not expose an `active` query filter.

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

`firstName`, `lastName` and `birthDate` are required; `middleName` is optional. Name fields have a maximum length of 100 characters. Required names cannot be blank; `birthDate` must be today or earlier (`yyyy-MM-dd`). Names are trimmed, blank middle names become `null`, and new readers are active by default.

**Partial update — `PATCH /api/readers/1`:**

```json
{
  "middleName": null,
  "lastName": "O'Connor"
}
```

Optional middle names can be cleared. Required names and birth dates cannot be set to `null`. Successful creation/update returns a `ReaderDTO`, for example:

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

A reader cannot be deactivated while they have an open borrowing record (`409 Conflict`). Inactive readers cannot borrow books.

### Borrows

Each borrow links a physical copy and a reader. A record is **open** while `returnDate` is `null`. Returning a book updates the same record and preserves its history.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/borrows` | List records; optionally filter by `readerId`, `bookId` and `returned` |
| `GET` | `/api/borrows/{id}` | Retrieve a record |
| `POST` | `/api/borrows` | Borrow a copy for a reader |
| `PATCH` | `/api/borrows/{id}/return` | Return a borrowed copy; no body |

Filters use AND when combined. `returned=true` selects returned borrows, `returned=false` selects open borrows, and an omitted `returned` includes both. An empty collection returns `[]`.

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

Both IDs are required. The book and reader must exist and be active, and the copy must not have another open borrow. `borrowDate` is set by the server. A successful response (`201 Created`) resembles:

```json
{
  "id": 18,
  "bookId": 5,
  "readerId": 12,
  "borrowDate": "2026-10-27",
  "returnDate": null
}
```

**Return — `PATCH /api/borrows/18/return`:** Sets `returnDate` to the current date and returns the updated DTO (`200 OK`). Returning an already returned record gives `409 Conflict`. A duplicate open borrow or an inactive book/reader also causes `409 Conflict`. Nonexistent resources cause `404 Not Found`.

The database `uq_open_borrow` constraint prevents two simultaneous **open** borrowing records for one copy. Broader concurrency scenarios and API-level conflict handling are planned for later verification.

### Logs — audit history

The Logs module automatically persists important business events triggered by the Books, Readers and Borrows services. These are **business audit records**, distinct from application console/SLF4J logs.

| Event type | Trigger |
|---|---|
| `BOOK_CREATED` | A copy is created |
| `BOOK_UPDATED` | Editable book details change |
| `BOOK_ACTIVATED` | A book is activated |
| `BOOK_DEACTIVATED` | A book is deactivated |
| `READER_CREATED` | A reader is registered |
| `READER_UPDATED` | Editable reader details change |
| `READER_ACTIVATED` | A reader is activated |
| `READER_DEACTIVATED` | A reader is deactivated |
| `BOOK_BORROWED` | A book is borrowed |
| `BOOK_RETURNED` | A borrowed book is returned |

**Audit behavior:**

- Each record contains a timestamp, event type, readable description, and optional `readerId` and/or `bookId` references.
- Update descriptions show previous and new values. No-op PATCH requests do not create `BOOK_UPDATED`/`READER_UPDATED` events.
- Related business changes and audit writes use the same transaction; a failure during audit writing rolls back the business change.
- The injected `Clock` is configured for `Europe/Warsaw`. The API exposes `LocalDateTime` without a UTC offset.
- Logs are exposed through read endpoints only; there is no public API for creating, editing or deleting audit records directly.
- **Not yet implemented:** attribution of an audit event to the authenticated staff member.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/logs` | Retrieve all audit events |
| `GET` | `/api/logs/readers/{id}` | Retrieve events related to a reader |
| `GET` | `/api/logs/books/{id}` | Retrieve events related to a book |

Every endpoint requires the `ADMIN` role and supports pagination:

| Parameter | Default | Valid values | Meaning |
|---|---|---|---|
| `page` | `0` | `0` or greater | Zero-based page number |
| `limit` | `30` | `5`–`100` | Page size |

Results are sorted by **`date DESC`, then `id DESC`**. Invalid pagination parameters result in `400 Bad Request`.

```http
GET /api/logs?page=0&limit=30
GET /api/logs/readers/12?page=0&limit=10
GET /api/logs/books/5?page=1&limit=20
```

Example Spring Data `PagedModel` response (illustrative data):

```json
{
  "content": [
    {
      "id": 42,
      "date": "2026-10-28T17:30:00",
      "eventType": "BOOK_BORROWED",
      "description": "Book The Hobbit borrowed by reader John Smith.",
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

Book-only events have `readerId: null`; reader-only events have `bookId: null`. Borrow/return events reference both. An empty page still returns pagination metadata. These endpoints do not independently check whether a referenced book/reader exists; an unknown ID without matching logs returns `200 OK` and an empty page.

### Staff — account management

Staff accounts are **not** library readers. Only `ADMIN` can access these endpoints. Public self-registration is not supported.

| Method | Endpoint | Operation |
|---|---|---|
| `POST` | `/api/staff` | Create an enabled `LIBRARIAN` account |
| `PATCH` | `/api/staff/{id}/enable` | Enable a disabled staff account |
| `PATCH` | `/api/staff/{id}/disable` | Disable an enabled librarian account |

**Create — `POST /api/staff`:**

```json
{
  "username": "librarian1",
  "password": "ExampleStrongPassword123!"
}
```

The server creates accounts with `LIBRARIAN` role; the caller cannot select a different role. Usernames are trimmed before storage and must be unique (maximum 100 characters). Passwords require at least 12 characters and, with the current BCrypt implementation, are validated against a **maximum of 72 UTF-8 bytes** before hashing. A multi-byte character can occupy more than one byte. Raw passwords and password hashes are **not** included in API responses.

Example `201 Created` response:

```json
{
  "id": 2,
  "username": "librarian1",
  "role": "LIBRARIAN",
  "enabled": true
}
```

Enable/disable calls return the updated `StaffAccountDTO` with status `200 OK`. A nonexistent account gives `404 Not Found`. A duplicate username, repeated enable/disable operation, or an attempt to disable an `ADMIN` account gives `409 Conflict` in the corresponding handled cases.

A unique database constraint enforces usernames. **Concurrent account-creation conflicts have not yet received dedicated testing/exception translation**, so a constraint violation occurring during a race may not produce the same `409` as a normal duplicate-username request. Concurrency work is planned separately.

### Responses and errors

Handled API and security errors use Spring `ProblemDetail`. Security error responses are returned by the Spring Security filter chain; business errors are handled by `GlobalExceptionHandler`.

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid JSON, request/validation data or log pagination parameters |
| `401 Unauthorized` | Missing/invalid HTTP Basic credentials or disabled account |
| `403 Forbidden` | Authenticated staff member lacks the required role |
| `404 Not Found` | Requested book, reader, borrow or staff account does not exist |
| `409 Conflict` | Violated business rule, duplicate username or incompatible activation state |
| `500 Internal Server Error` | Unexpected server-side error |

Example `401` response (the actual response includes standard `ProblemDetail` fields):

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Authentication is required or credentials are invalid."
}
```

Example `403` response:

```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "You do not have permission to access this resource."
}
```

**Privacy and deployment note:** Reader details and audit descriptions can contain personal information. Do not publish this learning API over plain HTTP or populate an internet-accessible instance with real personal data without a deployment security review.

## Database

The fresh-install schema is in [`database/db_DDL_schema.sql`](database/db_DDL_schema.sql).

| Table | Purpose |
|---|---|
| `books` | Physical copies and active status |
| `readers` | Reader records and active status |
| `borrows` | Borrow/return history and related foreign keys |
| `logs` | Transactional audit events and optional book/reader references |
| `staff_accounts` | Staff username, BCrypt password hash, role and enabled status |

The `borrows` table uses a generated `open_book_id` with a uniqueness constraint to prevent multiple open borrows for one book. It also enforces valid borrow/return dates.

`staff_accounts` has an auto-generated `id`, unique `username`, `password_hash`, role stored as `VARCHAR(20)` (`ADMIN` or `LIBRARIAN`), and `enabled` defaulting to `1`. The JPA role mapping uses a string enum with a matching column length of 20.

The `logs` table stores an event type, timestamp, description and optional references. It does not currently contain a staff account ID.

**Schema upgrades:** An existing installation may require both the earlier logs schema update (`event_type`, `description`) and the new `staff_accounts` table. Because `ddl-auto=validate` does not migrate databases, update existing schemas explicitly. A migration tool such as Flyway is planned for a later stage.

## Tests

The existing automated suite covers business services, MVC controllers and MVC-to-MySQL integration scenarios. **All 293 existing tests passed locally after adapting them to Spring Security** (as reported during the current development stage). Dedicated staff/security tests are planned for the next stage.

| Tests | Scope | Infrastructure |
|---|---|---|
| `BookServiceTest`, `ReaderServiceTest`, `BorrowServiceTest` | Business rules, mapping and service collaboration | JUnit 5 + Mockito |
| `LogDescriptionBuilderTest`, `LogServiceTest` | Log descriptions, persistence interactions, sorting and pagination | JUnit 5 + Mockito |
| `BookControllerTest`, `ReaderControllerTest`, `BorrowControllerTest`, `LogControllerTest` | Routing, validation, JSON and business errors | `@WebMvcTest` + MockMvc + mocked services |
| `BookIntegrationTest`, `ReaderIntegrationTest`, `BorrowIntegrationTest` | Business flows, persistence, audit history and conflicts | `@SpringBootTest` + MockMvc + MySQL Testcontainers |
| `LogIntegrationTest` | Real queries, filtering, sorting and pagination | `@SpringBootTest` + MySQL Testcontainers |
| `TransactionRollbackIntegrationTest` | Rollback when the audit-description builder fails | `@SpringBootTest` + MySQL Testcontainers + targeted Mockito override |

### Security in the existing test suite

- MVC controller slice tests use `@AutoConfigureMockMvc(addFilters = false)` to focus on controller behavior, **not** authorization.
- Integration tests inherit `@WithMockUser(username = "integration-admin", roles = "ADMIN")` from `BaseIntegrationTest`, keeping security filters active while supplying a mock authenticated admin.
- `@WithMockUser` does not read staff accounts from MySQL and does not verify credentials. It is **not a substitute** for real authentication tests.
- Authentication, role restrictions, account disabling and the `401`/`403` response format were manually checked in Postman; automated security coverage remains to be added.

### Integration test infrastructure

- Tests use a temporary **MySQL 8.4** database started by Testcontainers, independent of the local development database.
- `MySqlTestConfiguration` declares the container as a Spring test bean with `@ServiceConnection`.
- The container initializes the schema from [`src/test/resources/schema_test.sql`](src/test/resources/schema_test.sql); keep it synchronized with the production DDL until migrations are introduced.
- `BaseIntegrationTest` provides shared repositories, MockMvc and foreign-key-safe cleanup.
- Rollback tests deliberately mock `LogDescriptionBuilder`; the controller, services, transactions, repositories and MySQL remain real.
- Tests that share a database should not be run in parallel without additional isolation.

### Running tests

**Requirements:** JDK 21 and a running Docker Engine (e.g. Docker Desktop) for integration tests. The necessary images may be downloaded on first use.

**Windows (PowerShell):**

```powershell
.\mvnw.cmd clean test
```

**Linux / macOS:**

```bash
./mvnw clean test
```

Unit tests and MVC slice tests do not need Docker; integration tests do. Testcontainers supplies its own MySQL connection, so local `DBUSER` and `DBPASSWORD` are not needed for the integration-test database. The test command does not build or run an application Docker image.

## Next steps

1. **Automated Spring Security and Staff tests:** service and MVC tests, authorization rules, HTTP Basic with database-backed users, enabled/disabled accounts, and `401`/`403` responses.
2. **Docker deployment:** application `Dockerfile`, `.dockerignore`, Docker Compose for application + MySQL, environment configuration and a controlled way to provision the first administrator in a new environment.
3. **Later improvements:** concurrency and race-condition tests/handling (including staff usernames), schema migrations (Flyway), CI with GitHub Actions, and optionally attributing audit records to authenticated staff members.
