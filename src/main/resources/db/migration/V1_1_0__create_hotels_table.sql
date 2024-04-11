CREATE SEQUENCE hotels_seq;

CREATE TABLE hotels
(
    id       bigint check (id > 0) NOT NULL DEFAULT NEXTVAL('hotels_seq'),
    name     varchar(255)          NOT NULL,
    address  text                  NOT NULL,
    timezone text                  NOT NULL,
    vat      decimal(8, 2)         NOT NULL,
    currency character(3)          NOT NULL,
    PRIMARY KEY (id)
);
