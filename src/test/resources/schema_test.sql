create table books
(
    id               int auto_increment
        primary key,
    title            varchar(255)         not null,
    author           varchar(255)         null,
    release_year     int                  null,
    publishing_house varchar(255)         null,
    active           tinyint(1) default 1 not null
);

create table readers
(
    id          int auto_increment
        primary key,
    first_name  varchar(100)         not null,
    middle_name varchar(100)         null,
    last_name   varchar(100)         not null,
    birth_date  date                 not null,
    active      tinyint(1) default 1 not null
);

create table borrows
(
    id           int auto_increment
        primary key,
    book_id      int                      not null,
    reader_id    int                      not null,
    borrow_date  date default (curdate()) not null,
    return_date  date                     null,
    open_book_id int as (if((`return_date` is null), `book_id`, NULL)) stored,
    constraint uq_open_borrow
        unique (open_book_id),
    constraint fk_borrows_books
        foreign key (book_id) references books (id),
    constraint fk_borrows_readers
        foreign key (reader_id) references readers (id),
    constraint chk_borrow_dates
        check ((`return_date` is null) or (`return_date` >= `borrow_date`))
);

create table logs
(
    id          int auto_increment
        primary key,
    date        datetime default CURRENT_TIMESTAMP not null,
    event_type  varchar(50)                        not null,
    description text                               not null,
    reader_id   int                                null,
    book_id     int                                null,
    constraint fk_logs_books
        foreign key (book_id) references books (id),
    constraint fk_logs_readers
        foreign key (reader_id) references readers (id)
);

create table staff_accounts
(
    id            int auto_increment
        primary key,
    username      varchar(100)         not null,
    password_hash varchar(255)         not null,
    role          varchar(20)          not null,
    enabled       tinyint(1) default 1 not null,
    constraint username
        unique (username),
    constraint chk_staff_role
        check (`role` in ('ADMIN','LIBRARIAN'))
);

