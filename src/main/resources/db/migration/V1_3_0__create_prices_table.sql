CREATE TABLE prices
(
    room_id  bigint check (room_id > 0) NOT NULL,
    date     date                       NOT NULL,
    price    decimal                    NOT NULL,
    PRIMARY KEY (date, room_id),
    CONSTRAINT prices_room_id_foreign FOREIGN KEY (room_id) REFERENCES rooms (id) ON DELETE CASCADE
);

CREATE INDEX prices_room_id_foreign ON prices (room_id);
