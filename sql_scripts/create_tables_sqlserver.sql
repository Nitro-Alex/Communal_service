-- ==========================================
-- Таблица домов
-- ==========================================

CREATE TABLE building (

    building_id INT IDENTITY(1,1) PRIMARY KEY,

    address VARCHAR(100) NOT NULL,

    apartments_count INT NOT NULL,

    CONSTRAINT chk_building_apartments
        CHECK (apartments_count > 0)
);


-- ==========================================
-- Таблица категорий льгот
-- ==========================================

CREATE TABLE benefit_type (

    category VARCHAR(50) PRIMARY KEY,

    discount_percent DECIMAL(5,2) NOT NULL,

    CONSTRAINT chk_discount
        CHECK (
            discount_percent >= 0
            AND
            discount_percent <= 100
        )
);


-- ==========================================
-- Таблица коммунальных услуг
-- ==========================================

CREATE TABLE service (

    name VARCHAR(50) PRIMARY KEY,

    unit VARCHAR(20) NOT NULL,

    price DECIMAL(10,2) NOT NULL,

    CONSTRAINT chk_service_price
        CHECK(price >= 0)
);


-- ==========================================
-- Таблица квартир
-- ==========================================

CREATE TABLE apartment (

    apartment_id INT IDENTITY(1,1) PRIMARY KEY,

    building_id INT NOT NULL,

    apartment_number INT NOT NULL,

    area DECIMAL(10,2) NOT NULL,

    CONSTRAINT chk_area
        CHECK(area > 0),

    CONSTRAINT fk_apartment_building
        FOREIGN KEY(building_id)
        REFERENCES building(building_id),

    CONSTRAINT uq_building_apartment
        UNIQUE(building_id, apartment_number)
);


-- ==========================================
-- Таблица жильцов
-- ==========================================

CREATE TABLE resident (

    resident_id INT IDENTITY(1,1) PRIMARY KEY,

    last_name VARCHAR(50) NOT NULL,

    first_name VARCHAR(50) NOT NULL,

    apartment_id INT NOT NULL UNIQUE,

    category VARCHAR(50) NULL,

    CONSTRAINT fk_resident_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES apartment(apartment_id),

    CONSTRAINT fk_resident_benefit
        FOREIGN KEY(category)
        REFERENCES benefit_type(category)
);


-- ==========================================
-- Таблица показаний
-- ==========================================

CREATE TABLE reading (

    reading_id INT IDENTITY(1,1) PRIMARY KEY,

    resident_id INT NOT NULL,

    service_name VARCHAR(50) NOT NULL,

    [month] INT NOT NULL,

    [year] INT NOT NULL,

    value DECIMAL(10,2) NOT NULL,

    charge DECIMAL(10,2) NOT NULL,

    CONSTRAINT chk_month
        CHECK([month] BETWEEN 1 AND 12),

    CONSTRAINT chk_year
        CHECK([year] >= 2000),

    CONSTRAINT chk_value
        CHECK(value >= 0),

    CONSTRAINT chk_charge
        CHECK(charge >= 0),

    CONSTRAINT fk_reading_resident
        FOREIGN KEY(resident_id)
        REFERENCES resident(resident_id),

    CONSTRAINT fk_reading_service
        FOREIGN KEY(service_name)
        REFERENCES service(name),

    CONSTRAINT uq_reading
        UNIQUE(
            resident_id,
            service_name,
            [month],
            [year]
        )
);


-- ==========================================
-- Таблица оплат
-- ==========================================

CREATE TABLE payment (

    payment_id INT IDENTITY(1,1) PRIMARY KEY,

    resident_id INT NOT NULL,

    payment_date DATE NOT NULL,

    amount DECIMAL(10,2) NOT NULL,

    CONSTRAINT chk_amount
        CHECK(amount > 0),

    CONSTRAINT fk_payment_resident
        FOREIGN KEY(resident_id)
        REFERENCES resident(resident_id)
);