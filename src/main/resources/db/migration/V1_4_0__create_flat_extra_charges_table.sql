CREATE SEQUENCE extra_charges_flat_seq;

CREATE TYPE charge_type AS ENUM ('PER_NIGHT','ONCE');

CREATE TABLE extra_charges_flat
(
    id          bigint check (id > 0)       NOT NULL DEFAULT NEXTVAL('extra_charges_flat_seq'),
    hotel_id    bigint check (hotel_id > 0) NOT NULL,
    description text                        NOT NULL,
    charge_type charge_type                 NOT NULL,
    price       decimal                     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT extra_charges_flat_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
);

CREATE INDEX extra_charges_flat_hotel_id_foreign ON extra_charges_flat (hotel_id);
