-- This script contains SQL insert commands to populate database tables with initial data
-- for local testing purposes.
--
-- TIP - You can populate the database by running following command: (Assuming you ran the docker compose file)
-- ```sh
--   cat local/inserts.sql | docker exec -i katanox_test_postgresdb psql -U katanox-user -d katanox_test
-- ```
INSERT INTO HOTELS
    (id, name, address, timezone, vat, currency)
VALUES (1, 'Hotel A', 'Amsterdam', 'CET', 0.1, 'EUR'),
       (2, 'Hotel B', 'New York', 'EDT', 0.2, 'USD');

INSERT INTO ROOMS
    (id, hotel_id, name, description, quantity)
VALUES (1, 1, 'Room A1', 'Single room', 2),
       (2, 1, 'Room A2', 'Double room', 4),
       (3, 2, 'Room B1', 'Single room', 5),
       (4, 2, 'Room B2', 'Triple room', 1);

INSERT INTO PRICES
    (room_id, date, price)
VALUES (1, '2022-04-01', 103),
       (1, '2022-04-02', 99),
       (1, '2022-04-03', 110),
       (2, '2022-04-01', 113),
       (2, '2022-04-02', 109),
       (2, '2022-04-03', 123),
       (3, '2022-04-01', 150),
       (3, '2022-04-02', 200),
       (4, '2022-04-02', 550),
       (4, '2022-04-03', 200);

INSERT INTO EXTRA_CHARGES_FLAT
    (id, hotel_id, description, charge_type, price)
VALUES (1, 1, 'Cleaning Fee', 'ONCE', 25),
       (2, 1, 'WiFi Wavelengths Fee', 'PER_NIGHT', 5),
       (3, 2, 'Snacks Fee', 'ONCE', 20),
       (4, 2, 'Open Window Fee', 'ONCE', 3);

INSERT INTO EXTRA_CHARGES_PERCENTAGE
    (id, hotel_id, description, applied_on, percentage)
VALUES (1, 1, 'Staying fee', 'FIRST_NIGHT', 10),
       (2, 2, 'Safety fee', 'FIRST_NIGHT', 15),
       (3, 2, 'Leaving fee', 'TOTAL_AMOUNT', 5);
