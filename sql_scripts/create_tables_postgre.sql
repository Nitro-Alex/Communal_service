-- Таблица домов
CREATE TABLE building (

    building_id SERIAL PRIMARY KEY,

    address VARCHAR(100) NOT NULL,

    apartments_count INTEGER NOT NULL
        CHECK(apartments_count > 0)
);

-- Таблица льгот
CREATE TABLE benefit_type (

    category VARCHAR(50) PRIMARY KEY,

    discount_percent NUMERIC(5,2) NOT NULL
        CHECK(
            discount_percent >=0
            AND
            discount_percent <=100
        )
);

-- Таблица услуг
CREATE TABLE service (

    name VARCHAR(50) PRIMARY KEY,

    unit VARCHAR(20) NOT NULL,

    price NUMERIC(10,2) NOT NULL
        CHECK(price>=0)
);

-- Таблица квартир
CREATE TABLE apartment (

    apartment_id SERIAL PRIMARY KEY,

    building_id INTEGER NOT NULL,

    apartment_number INTEGER NOT NULL,

    area NUMERIC(10,2) NOT NULL
        CHECK(area>0),

    CONSTRAINT fk_apartment_building
        FOREIGN KEY(building_id)
        REFERENCES building(building_id),

    CONSTRAINT uq_apartment
        UNIQUE(
            building_id,
            apartment_number
        )
);

-- Таблица жильцов
CREATE TABLE resident (

    resident_id SERIAL PRIMARY KEY,

    last_name VARCHAR(50) NOT NULL,

    first_name VARCHAR(50) NOT NULL,

    apartment_id INTEGER NOT NULL UNIQUE,

    category VARCHAR(50),

    CONSTRAINT fk_resident_apartment
        FOREIGN KEY(apartment_id)
        REFERENCES apartment(apartment_id),

    CONSTRAINT fk_resident_benefit
        FOREIGN KEY(category)
        REFERENCES benefit_type(category)
);

-- Таблица показаний
CREATE TABLE reading (

    reading_id SERIAL PRIMARY KEY,

    resident_id INTEGER NOT NULL,

    service_name VARCHAR(50) NOT NULL,

    month INTEGER NOT NULL
        CHECK(month BETWEEN 1 AND 12),

    year INTEGER NOT NULL
        CHECK(year>=2000),

    value NUMERIC(10,2) NOT NULL
        CHECK(value>=0),

    charge NUMERIC(10,2) NOT NULL
        CHECK(charge>=0),

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
            month,
            year
        )
);

-- Таблица оплат
CREATE TABLE payment (

    payment_id SERIAL PRIMARY KEY,

    resident_id INTEGER NOT NULL,

    payment_date DATE NOT NULL,

    amount NUMERIC(10,2) NOT NULL
        CHECK(amount>0),

    CONSTRAINT fk_payment_resident
        FOREIGN KEY(resident_id)
        REFERENCES resident(resident_id)
);