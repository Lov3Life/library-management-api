# Library Management API

A REST API built with **Java 21** and **Spring Boot 4.1.1** for managing physical book copies, library readers, borrowing records, staff accounts, and an audit history of business events.

The application includes validated CRUD-style operations, partial updates, activation/deactivation rules, borrowing and returns, paginated audit logs, and **Spring Security authentication with role-based authorization**.

## Project status

**Completed educational backend project.** The API includes the following implemented and tested modules:

| Module | Features |
|---|---|
| Books | Catalog management, validation, partial updates, activation and deactivation |
| Readers | Reader management, filtering, validation, activation and deactivation |
| Borrows | Borrowing, returns, history and business rules |
| Logs | Transactional audit history, descriptions and paginated queries |
| Staff | Librarian accounts, listing, creation, activation and deactivation |
| Security | HTTP Basic authentication, BCrypt password hashing and role-based authorization |
| Tests & CI | 355 tests passing locally and in GitHub Actions |

[![CI - Maven Tests](https://github.com/Lov3Life/library-management-api/actions/workflows/ci.yml/badge.svg)](https://github.com/Lov3Life/library-management-api/actions/workflows/ci.yml)

**Tech stack:** Java 21, Spring Boot 4.1.1, Spring Web MVC, Spring Security, Spring Data JPA / Hibernate, MySQL, Jakarta Bean Validation, Maven, and Jackson's `JsonNullable` for partial updates.

**Testing:** JUnit 5, Mockito, MockMvc, Spring Boot Test, Spring Security Test, Testcontainers and MySQL 8.4. Docker is used by Testcontainers for integration tests; the application itself is run locally without container packaging.

## Getting started

### Requirements

- JDK 21.
- MySQL server (default: `localhost:3306`).
- Maven Wrapper included in the repository; no separate Maven installation is required.

Docker is not required to run the API locally. It is required for database integration tests using Testcontainers.

### 1. Prepare MySQL

Create a database named `library_management` and execute [`database/db_DDL_schema.sql`](database/db_DDL_schema.sql) to create the `books`, `readers`, `borrows`, `logs`, and `staff_accounts` tables.

Hibernate uses `spring.jpa.hibernate.ddl-auto=validate`. It checks the database schema at startup rather than creating or modifying tables.

### 2. Configure environment variables

| Variable | When needed | Purpose |
|---|---|---|
| `DBUSER` | Application startup | MySQL username |
| `DBPASSWORD` | Application startup | MySQL password |
| `SPRING_PROFILES_ACTIVE` | Administrator bootstrap | Set to `dev` |
| `ADMIN_USERNAME` | With `dev` profile | Initial administrator username |
| `ADMIN_PASSWORD` | With `dev` profile | Initial administrator password (minimum 12 characters) |

Set these variables in your shell or IntelliJ IDEA run configuration. Do not commit passwords or files containing credentials.

### 3. Initialize an administrator

The `DevAdminBootstrap` component is enabled by the `dev` Spring profile. It creates an enabled `ADMIN` account if the configured username is not already present, storing the password as a BCrypt hash.

To initialize an administrator, set `SPRING_PROFILES_ACTIVE=dev`, `ADMIN_USERNAME`, and `ADMIN_PASSWORD` before launching the application.

The API has no public staff-registration endpoint. Administrators can create librarian accounts using `POST /api/staff`.

### 4. Run the application

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**

```bash
./mvnw spring-boot:run
```

Default base URL: `http://localhost:8080`.

`GET /` is public and returns `200 OK` with the body `OK`. API endpoints for books, readers, borrows, logs and staff require authentication.

### 5. Authenticate requests

The API uses **HTTP Basic authentication**. In Postman, select **Authorization → Basic Auth** and enter an enabled staff account's username and password. There is no `/login` endpoint or JWT token exchange.

Example using curl (the password is prompted interactively):

```bash
curl --user admin http://localhost:8080/api/books
```

HTTP Basic sends credentials with requests, so HTTPS is necessary if the API is ever exposed outside a trusted local environment.

## Authentication and authorization

Staff accounts are stored in MySQL (`staff_accounts`). A custom `UserDetailsService` loads their credentials and authorities; `BCryptPasswordEncoder` verifies passwords. Disabled accounts cannot authenticate. Requests use a stateless security configuration (`SessionCreationPolicy.STATELESS`).

There are two staff roles:

- `ADMIN` — can manage books, readers, borrows, audit logs and staff accounts.
- `LIBRARIAN` — can manage books, readers and borrows, but cannot access audit logs or staff management.

The configuration uses `ROLE_ADMIN` and `ROLE_LIBRARIAN` authorities. Unmatched endpoint paths are denied by default.

| Resource | Anonymous | `LIBRARIAN` | `ADMIN` |
|---|:---:|:---:|:---:|
| `GET /` | Allowed | Allowed | Allowed |
| `/api/books` and `/api/books/**` | Denied | Allowed | Allowed |
| `/api/readers` and `/api/readers/**` | Denied | Allowed | Allowed |
| `/api/borrows` and `/api/borrows/**` | Denied | Allowed | Allowed |
| `/api/logs` and `/api/logs/**` | Denied | Denied | Allowed |
| `/api/staff` and `/api/staff/**` | Denied | Denied | Allowed |

For protected endpoints:

- **`401 Unauthorized`** — missing or invalid HTTP Basic credentials, or a disabled account.
- **`403 Forbidden`** — an authenticated user lacks the required role.

Custom `AuthenticationEntryPoint` and `AccessDeniedHandler` implementations return Spring `ProblemDetail` responses (`Content-Type: application/problem+json`). A `401` HTTP Basic response includes the `WWW-Authenticate` header.

**Automated security verification is implemented:** tests cover HTTP Basic with database-backed accounts, the role-access matrix, invalid credentials, disabled accounts, staff creation and account activation/deactivation flows. See [Tests](#tests).

**Security scope:** CSRF protection is disabled for this stateless API. Authorization is based on the two staff roles. Audit records do not identify the authenticated staff member who performed an operation.

## API

Unless specified otherwise, retrievals and updates return `200 OK`; creation returns `201 Created`. Book and reader identifiers are database-generated. Their active status is changed through dedicated endpoints.

**Books and Readers PATCH behavior:** Omitted fields remain unchanged. Explicit `null` is accepted only for optional fields. An empty `{}` does not change the resource. No-op updates do not generate audit events. The `active` field cannot be modified through the general PATCH endpoint.

### Books

One row in `books` represents **one physical copy** of a book, not a title shared across multiple copies.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/books` | List books; optionally filter by `title` and `active` |
| `GET` | `/api/books/{id}` | Retrieve a book |
| `POST` | `/api/books` | Create a book |
| `PATCH` | `/api/books/{id}` | Update selected fields |
| `PATCH` | `/api/books/{id}/activate` | Activate a book |
| `PATCH` | `/api/books/{id}/deactivate` | Deactivate a book |

The `title` filter performs a **case-insensitive substring search**. Leading/trailing whitespace is ignored; blank input applies no filter. `active` can be `true` or `false`; when omitted, both states are returned.

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

Only `title` is required and cannot be blank. `author`, `releaseYear`, and `publishingHouse` are optional. Text fields are trimmed; blank optional text is converted to `null`. Books are active by default.

**Partial update — `PATCH /api/books/1`:**
```json
{
  "author": null,
  "releaseYear": null
}
```

This clears the author and release year without changing other fields. `title` cannot be cleared. Create/update responses contain the full `BookDTO`, including `id` and `active`.

A book cannot be deactivated while it has an **open borrowing record** (`returnDate == null`); attempting this returns `409 Conflict`. An inactive book cannot be borrowed.

### Readers

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/readers` | List readers; optionally filter by `firstName` and `lastName` |
| `GET` | `/api/readers/{id}` | Retrieve a reader |
| `POST` | `/api/readers` | Register a reader |
| `PATCH` | `/api/readers/{id}` | Update selected fields |
| `PATCH` | `/api/readers/{id}/activate` | Activate a reader |
| `PATCH` | `/api/readers/{id}/deactivate` | Deactivate a reader |

Name filters are **case-insensitive substring searches**; inputs are trimmed and both filters are combined with AND. Omitted or blank filters do not restrict results. The endpoint does not expose an `active` query filter.

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

`firstName`, `lastName` and `birthDate` are required. `middleName` is optional. Name fields have a maximum length of 100 characters. Required names cannot be blank; `birthDate` must be today or earlier (`yyyy-MM-dd`). Names are trimmed, and a blank middle name is converted to `null`. New readers are active by default.

**Partial update — `PATCH /api/readers/1`:**

```json
{
  "middleName": null,
  "lastName": "O'Connor"
}
```

Optional middle names can be cleared. Required names and birth dates cannot be set to `null`. A successful create/update response resembles:

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

Each borrow record links one physical copy to a reader. The record is **open** while `returnDate` is `null`. Returning a book updates the same record, preserving the borrowing history.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/borrows` | List records; filter by `readerId`, `bookId`, `returned` |
| `GET` | `/api/borrows/{id}` | Retrieve a record |
| `POST` | `/api/borrows` | Borrow a copy for a reader |
| `PATCH` | `/api/borrows/{id}/return` | Return a borrowed copy; no body |

Filters combine with AND. `returned=true` selects returned records, `returned=false` selects open records, and omitting `returned` includes both. Empty collections return `[]`.

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

Both IDs are required. The book and reader must exist and be active, and the copy must not have another open borrowing record. `borrowDate` is set by the server. Successful requests return `201 Created`:

```json
{
  "id": 18,
  "bookId": 5,
  "readerId": 12,
  "borrowDate": "2026-10-27",
  "returnDate": null
}
```

**Return — `PATCH /api/borrows/18/return`:** Sets `returnDate` to the current date and returns `200 OK` with the updated DTO. Returning an already returned record, borrowing an inactive copy, or creating a duplicate open borrow gives `409 Conflict`. Nonexistent resources give `404 Not Found`.

The `uq_open_borrow` database constraint prevents two simultaneous **open** borrowing records for one copy.

### Logs — audit history

The Logs module automatically persists events from the Books, Readers and Borrows services. These are **audit records**, not application console/SLF4J log messages.

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

- Each record has a timestamp, event type, human-readable description and optional `readerId`/`bookId` references.
- Update descriptions show the previous and new values. No-op PATCH requests do not generate `BOOK_UPDATED` or `READER_UPDATED` events.
- Business changes and audit writes participate in the same transaction; an audit failure rolls back the related business change.
- The injected `Clock` is configured for `Europe/Warsaw`. The API exposes `LocalDateTime` without a UTC offset.
- The public API exposes **read-only** log endpoints; audit records cannot be created, edited or deleted directly through the API.
- Staff-account actions are not currently included in the audit-event types; audit events also do not identify the authenticated staff actor.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/logs` | Retrieve all audit events |
| `GET` | `/api/logs/readers/{id}` | Retrieve events related to a reader |
| `GET` | `/api/logs/books/{id}` | Retrieve events related to a book |

All log endpoints require `ADMIN` and support pagination:

| Parameter | Default | Valid values | Meaning |
|---|---|---|---|
| `page` | `0` | `0` or greater | Zero-based page index |
| `limit` | `30` | `5`–`100` | Page size |

Results are sorted by **`date DESC`, then `id DESC`**. Invalid pagination arguments return `400 Bad Request`.

```http
GET /api/logs?page=0&limit=30
GET /api/logs/readers/12?page=0&limit=10
GET /api/logs/books/5?page=1&limit=20
```

Example Spring Data `PagedModel` response (illustrative values):

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

Book-only events have `readerId: null`; reader-only events have `bookId: null`. Borrow/return events reference both. Empty pages still return pagination metadata. Unknown book/reader IDs with no matching logs return an empty page with `200 OK`.

### Staff — account management

Staff accounts are **not** library readers. The endpoints below are accessible only to `ADMIN`. Public self-registration is not available.

| Method | Endpoint | Operation |
|---|---|---|
| `GET` | `/api/staff/librarians` | List all librarian accounts, including disabled ones |
| `POST` | `/api/staff` | Create an enabled `LIBRARIAN` account |
| `PATCH` | `/api/staff/{id}/enable` | Enable a disabled staff account |
| `PATCH` | `/api/staff/{id}/disable` | Disable an enabled librarian account |

**List librarians — `GET /api/staff/librarians`:** Returns `200 OK` and an array of `StaffAccountDTO` objects for `LIBRARIAN` accounts. Administrators are excluded. Both enabled and disabled librarians are included; an empty result is `[]`. Password hashes are never returned.

Example response:

```json
[
  {"id": 2, "username": "librarian1", "role": "LIBRARIAN", "enabled": true},
  {"id": 3, "username": "librarian2", "role": "LIBRARIAN", "enabled": false}
]
```

**Create — `POST /api/staff`:**

```json
{
  "username": "librarian1",
  "password": "ExampleStrongPassword123!"
}
```

New accounts always receive `LIBRARIAN`, regardless of caller identity. Usernames are trimmed, unique, non-blank, and at most 100 characters long. Passwords must be at least 12 characters and, for the current BCrypt implementation, at most **72 UTF-8 bytes**. A multi-byte character may occupy several bytes. Neither plaintext passwords nor password hashes appear in API responses.

Example `201 Created` response:

```json
{
  "id": 2,
  "username": "librarian1",
  "role": "LIBRARIAN",
  "enabled": true
}
```

Enable/disable operations return the updated `StaffAccountDTO` with `200 OK`. A nonexistent account gives `404 Not Found`; duplicate usernames, repeated enable/disable operations and attempts to disable an administrator give `409 Conflict`. The database also enforces username uniqueness.

### Responses and errors

Handled business and security errors use Spring `ProblemDetail`. Security errors are produced by the Spring Security filter chain; business errors are handled by `GlobalExceptionHandler`.

| Status | Meaning |
|---|---|
| `400 Bad Request` | Invalid JSON, validation or log pagination parameters |
| `401 Unauthorized` | Missing/invalid HTTP Basic credentials or disabled account |
| `403 Forbidden` | Authenticated account lacks the required role |
| `404 Not Found` | Requested book, reader, borrow or staff account does not exist |
| `409 Conflict` | Business-rule violation, duplicate username or incompatible activation state |
| `500 Internal Server Error` | Unexpected server-side error |

Example `401` response (`ProblemDetail`):

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

## Database

The database schema is defined in [`database/db_DDL_schema.sql`](database/db_DDL_schema.sql).

| Table | Purpose |
|---|---|
| `books` | Physical book copies and active status |
| `readers` | Reader records and active status |
| `borrows` | Borrow/return history with relationships to books and readers |
| `logs` | Transactional audit records and optional book/reader references |
| `staff_accounts` | Staff username, BCrypt password hash, role and enabled status |

The `borrows` table uses a generated `open_book_id` column and the unique index `uq_open_borrow` to prevent more than one open borrow per physical copy. Foreign keys protect relationships between records. Hibernate validates the schema at startup.

## Tests

The project has **355 automated tests**, all passing both locally and on GitHub Actions (**0 failures, 0 errors, 0 skipped**). Tests cover business logic, REST controllers, database integration, audit persistence and rollback behavior, authentication and authorization.

| Tests | Scope | Infrastructure |
|---|---|---|
| `BookServiceTest`, `ReaderServiceTest`, `BorrowServiceTest` | Business rules, DTO mapping and service dependencies | JUnit 5 + Mockito |
| `LogDescriptionBuilderTest`, `LogServiceTest` | Audit messages, service behavior, sorting and pagination | JUnit 5 + Mockito |
| `StaffAccountServiceTest` | Account creation, password encoding interactions, duplicate usernames, enabled/disabled transitions and administrator restrictions | JUnit 5 + Mockito |
| `CustomUserDetailsServiceTest` | Database user-to-`UserDetails` mapping, role authorities, disabled-account state and unknown users | JUnit 5 + Mockito |
| `BookControllerTest`, `ReaderControllerTest`, `BorrowControllerTest`, `LogControllerTest` | MVC routing, validation, JSON and error responses | `@WebMvcTest` + MockMvc + mocked services |
| `StaffAccountControllerTest` | Staff endpoint mappings, request validation, successful responses and business errors | `@WebMvcTest` + MockMvc + mocked service |
| `BookIntegrationTest`, `ReaderIntegrationTest`, `BorrowIntegrationTest` | Business flows, MySQL persistence, audit events and conflicts | `@SpringBootTest` + MockMvc + MySQL Testcontainers |
| `LogIntegrationTest` | Real queries, audit filtering, ordering and pagination | `@SpringBootTest` + MySQL Testcontainers |
| `TransactionRollbackIntegrationTest` | Rollback of business operations when audit generation fails | `@SpringBootTest` + MySQL Testcontainers + targeted mock override |
| `SecurityIntegrationTest` | Real HTTP Basic authentication, role access matrix, missing/invalid credentials, disabled accounts, staff POST/PATCH workflows and database-backed account state | `@SpringBootTest` + MockMvc + Spring Security Test + MySQL Testcontainers |

### Security testing approach

- MVC slice tests use `@AutoConfigureMockMvc(addFilters = false)` to focus on controller behavior and validation rather than access control.
- Business integration tests inherit `@WithMockUser(username = "integration-admin", roles = "ADMIN")` from `BaseIntegrationTest`; this supplies a simulated authenticated administrator while security filters remain active.
- **`SecurityIntegrationTest` does not use `@WithMockUser`**: it seeds real `ADMIN`/`LIBRARIAN` accounts in the test database, hashes their passwords with the configured `PasswordEncoder` and sends actual HTTP Basic credentials through MockMvc.
- Authentication and authorization are verified separately. The security tests cover anonymous requests, role restrictions, successful access, incorrect credentials, nonexistent users and disabled accounts.
- Staff workflow tests also exercise account creation, enabling/disabling and expected authorization for state-changing requests.
- A mocked user does not verify database-backed authentication or BCrypt; the dedicated integration tests exist specifically to cover that gap.

### Integration test infrastructure

- Integration tests use an isolated **MySQL 8.4** database managed by Testcontainers, not the local development database.
- `MySqlTestConfiguration` declares the container as a Spring test bean with `@ServiceConnection`.
- The container initializes its database using [`src/test/resources/schema_test.sql`](src/test/resources/schema_test.sql), which mirrors the application schema.
- `BaseIntegrationTest` supplies shared repositories, MockMvc and foreign-key-safe cleanup for business integration tests.
- Security tests manage their own staff-account fixtures and use the real Spring Security filter chain.
- Transaction rollback tests deliberately mock `LogDescriptionBuilder` while leaving controller, service, transaction manager, repositories and MySQL active.
- Tests that share a database should not be run in parallel without additional isolation.

### Running tests

**Requirements:** JDK 21 and Docker Desktop (or another running Docker Engine) for Testcontainers integration tests. The necessary images may need to be downloaded during the first run.

**Windows (PowerShell):**

```powershell
.\mvnw.cmd clean test
```

**Linux / macOS:**

```bash
./mvnw clean test
```

Unit and MVC slice tests do not need Docker; database integration tests do. Testcontainers supplies its own database connection, so local `DBUSER` and `DBPASSWORD` are not required for its MySQL instance.

## Continuous integration

GitHub Actions automatically runs the complete Maven test suite using [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

- **Triggers:** every push to `main` and every pull request targeting `main`.
- **Environment:** Ubuntu 24.04, Eclipse Temurin JDK 21, Maven Wrapper and Maven dependency caching.
- **Database tests:** Docker is available on the runner; Testcontainers starts an isolated MySQL 8.4 instance.
- **Command:** `bash ./mvnw --batch-mode clean verify`.

The workflow succeeds only when Maven completes successfully. Failed tests cause the job to fail.

This repository is a completed educational backend API.