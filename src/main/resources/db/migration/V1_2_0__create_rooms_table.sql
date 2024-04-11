CREATE SEQUENCE rooms_seq;

CREATE TABLE rooms
(
    id          bigint check (id > 0)       NOT NULL DEFAULT NEXTVAL('rooms_seq'),
    hotel_id    bigint check (hotel_id > 0) NOT NULL,
    name        varchar(255)                NOT NULL,
    description text                        NOT NULL,
    quantity    int                         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT room_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
);

CREATE INDEX room_hotel_id_foreign ON rooms (hotel_id);
