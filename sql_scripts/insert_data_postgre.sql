-- ==========================
-- Очистка существующих данных
-- ==========================

TRUNCATE TABLE reading RESTART IDENTITY CASCADE;
TRUNCATE TABLE payment RESTART IDENTITY CASCADE;
TRUNCATE TABLE resident RESTART IDENTITY CASCADE;
TRUNCATE TABLE apartment RESTART IDENTITY CASCADE;
TRUNCATE TABLE building RESTART IDENTITY CASCADE;
TRUNCATE TABLE service RESTART IDENTITY CASCADE;
TRUNCATE TABLE benefit_type RESTART IDENTITY CASCADE;


-- ==========================
-- benefit_type
-- ==========================

INSERT INTO benefit_type(category, discount_percent)
VALUES
('Малоимущий',20.00),
('Многодетный',40.00),
('Пенсионер',25.00),
('Инвалид',60.00),
('Ветеран',80.00);


-- ==========================
-- service
-- ==========================

INSERT INTO service(name, unit, price)
VALUES
('Холодная вода','м^3',3),
('Горячая вода','м^3',5),
('Электричество','кВт*ч',0.5),
('Отопление','м^2',8);


-- ==========================
-- building
-- ==========================

INSERT INTO building(address, apartments_count)
VALUES
('ул. Ленина, д. 10', 120),
('пр. Победы, д. 25', 45),
('ул. Центральная, д. 7', 346),
('ул. Гагарина, д. 14', 230),
('ул. Молодежная, д. 3', 87);


-- ==========================
-- apartment
-- building_id: 1..5
-- ==========================

INSERT INTO apartment
(apartment_number, area, building_id)
VALUES
(12,58.5,1),
(45,73.2,1),
(8,49.0,2),
(22,65.4,3),
(15,82.7,4),
(31,90.1,5);


-- ==========================
-- resident
-- apartment_id: 1..6
-- ==========================

INSERT INTO resident
(
last_name,
first_name,
category,
apartment_id
)
VALUES
('Иванов','Алексей','Пенсионер',1),
('Петров','Сергей', NULL,2),
('Сидорова','Анна','Многодетный',3),
('Ковалев','Дмитрий','Инвалид',4),
('Соколова','Мария','Малоимущий',5),
('Смирнов','Игорь','Ветеран',6);


-- ==========================
-- reading
-- service_name:
-- Холодная вода
-- Горячая вода
-- Электричество
-- Отопление
-- ==========================

INSERT INTO reading
(
resident_id,
service_name,
year,
month,
value,
charge
)
VALUES

(1,'Холодная вода',2026,1,8.4,18.82),
(1,'Электричество',2026,1,190,45.60),

(2,'Горячая вода',2026,1,6.5,36.40),

(3,'Электричество',2026,2,240,46.08),

(4,'Отопление',2026,2,65.4,30.08),

(5,'Холодная вода',2026,3,9.1,20.38),

(6,'Отопление',2026,3,90.1,20.72),

(6,'Горячая вода',2026,3,11.4,12.77);


-- ==========================
-- payment
-- ==========================

INSERT INTO payment
(
resident_id,
payment_date,
amount
)
VALUES

(1,'2026-01-15',10.00),
(1,'2026-02-02',5.00),

(2,'2026-01-20',20.00),

(3,'2026-02-16',15.00),

(4,'2026-02-21',25.00),

(5,'2026-03-10',8.00),

(6,'2026-03-12',30.00),

(6,'2026-03-25',12.00);