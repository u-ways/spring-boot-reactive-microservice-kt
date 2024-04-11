CREATE SEQUENCE extra_charges_percentage_seq;

CREATE TYPE applied_on AS ENUM ('FIRST_NIGHT','TOTAL_AMOUNT');

CREATE TABLE extra_charges_percentage
(
    id          bigint check (id > 0)       NOT NULL DEFAULT NEXTVAL('extra_charges_percentage_seq'),
    hotel_id    bigint check (hotel_id > 0) NOT NULL,
    description text                        NOT NULL,
    applied_on  applied_on                  NOT NULL,
    percentage  decimal(8, 5)               NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT extra_charges_percentage_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
);

CREATE INDEX extra_charges_percentage_hotel_id_foreign ON extra_charges_percentage (hotel_id);
