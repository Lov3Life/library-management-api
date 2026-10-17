# Library-management-API

---

## Project Overview

Library Management API is a RESTful application built with Java 
and Spring Boot for managing books, readers, and borrowing 
operations. The system allows library staff to manage the 
book collection, register readers, track borrowing history, 
and process book returns. Each book record represents a 
single physical copy, which can only be borrowed by one reader 
at a time. Completed borrow records are preserved to maintain 
borrowing history. The application uses MySQL for data storage 
and Hibernate ORM for entity mapping. Business operations 
are recorded in an audit log.

---

## Database Design

The database has four tables: books, readers, borrows, and logs.
The database is written in MySQL.

### BOOKS

#### table structure

| Name | Type | Key | Null | Default |
|:---|:---|:---:|:---:|:---:|
|`id`|`INT`|`PK`| `No`|`auto_increment`|
|`title`|`VARCHAR(255)`|`-`| `NO`|`-`|
|`author`|`VARCHAR(255)`|`-`| `Yes`|`-`|
|`release_year`|`INT`|`-`| `Yes`|`-`|
|`publishing_house`|`VARCHAR(255)`|`-`| `Yes`|`-`|
|`active`|`TINYINT(1)`|`-`| `No`|`true`|

### READERS

#### table structure

| Name | Type | Key | Null | Default |
|:---|:---|:---:|:---:|:---:|
|`id`|`INT`|`PK`| `No`|`AUTO_INCREMENT`|
|`first_name`|`VARCHAR(100)`|`-`| `NO`|`-`|
|`middle_name`|`VARCHAR(100)`|`-`| `Yes`|`-`|
|`last_name`|`VARCHAR(100)`|`-`| `NO`|`-`|
|`birth_date`|`DATE`|`-`| `NO`|`-`|
|`active`|`TINYINT(1)`|`-`| `No`|`true`|

### BORROWS

#### table structure

| Name | Type | Key | Null | Default |
|:---|:---|:---:|:---:|:---:|
|`id`|`INT`|`PK`| `No`|`AUTO_INCREMENT`|
|`book_id`|`INT`|`FK`| `No`|`-`|
|`reader_id`|`INT`|`FK`| `No`|`-`|
|`borrow_date`|`DATE`|`-`| `No`|`CURRENT_DATE`|
|`return_date`|`DATE`|`-`| `YES`|`NULL`|
|`open_book_id`|`INT`|`UNIQUE`| `YES`|`GENERATED`|

### LOGS

#### table structure

| Name | Type | Key | Null | Default |
|:---|:---|:---:|:---:|:---:|
|`id`|`INT`|`PK`| `No`|`AUTO_INCREMENT`|
|`date`|`DATETIME`|`-`| `NO`|`CURRENT_TIMESTAMP`|
|`description`|`VARCHAR(255)`|`-`| `No`|`-`|
|`reader_id`|`INT`|`FK`| `YES`|`-`|
|`book_id`|`INT`|`FK`| `YES`|`-`|


---

## Planned API Endpoints

### Books

`GET /api/books?title={title}&active={active}` – List all books 
(parameterized {title}: books that match the title;
(parameterized {active}: books that are active - default true)) \
`GET /api/books/{id}` – Get a book by ID \
`POST /api/books` – Create a new book (body must have
a title other parameters are optional, id is not allowed) \
`PATCH /api/books/{id}` – Partially update a book (body must
not be empty and id as parameter is not allowed) \
`PATCH /api/books/{id}/activate` – Activate a book \
`PATCH /api/books/{id}/deactivate` – Deactivate a book

### Readers

`GET /api/readers?name={name}&surname={surname}` – List all readers
(parameterized: users who match the data) \
`GET /api/readers/{id}` – Get a reader by ID \
`POST /api/readers` – Create a new reader (body must have 
all parameters, exclude id) \
`PUT /api/readers/{id}` – Update a reader (body must not be empty
and id as parameter is not allowed) \
`PATCH /api/readers/{id}` – Partially update a reader (body must 
not be empty and id as parameter is not allowed) \

### Borrows

`GET /api/borrows?bookId={id}&readerId={id}` – list all currently 
borrowed books and his readers (parameterized: One request can not
have two parameters. bookId get a single book if it is borrowed.
readerId get a reader and all books borrowed by him) \
`GET /api/borrows/{id}` – Get a borrow by ID \
`POST /api/borrows` - Create a new borrow (body must have a reader 
and book ids. borrowID is not allowed in request body. Book can
not be borrowed if it is already borrowed) \
`PATCH /api/borrows/{id}` – Return a book (This endpoint changes 
borrow (by id) to expire. That means book not longer will be listed
as borrowed, and it can be borrowed again) 

### Logs

`GET /api/logs?limit={number}` – List of logs 
(default: 30 latest columns) \
`GET /api/logs/readers/{id}?limit={number}` – List of logs for a 
reader (default: 30 latest columns) \
`GET /api/logs/books/{id}?limit={number}` – List of logs for a 
book (default: 30 latest columns)