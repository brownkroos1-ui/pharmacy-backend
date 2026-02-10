CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    role VARCHAR(50),
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255),
    manufacturer VARCHAR(255),
    batch_number VARCHAR(255) NOT NULL UNIQUE,
    price DECIMAL(19, 2) NOT NULL,
    cost_price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL,
    reorder_level INT,
    expiry_date DATE NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    sale_date DATETIME,
    total_price DECIMAL(19, 2),
    status VARCHAR(50),
    CONSTRAINT fk_sales_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id)
);

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255),
    email VARCHAR(255),
    address VARCHAR(255),
    active TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS stock_ins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(19, 2),
    invoice_number VARCHAR(255),
    note VARCHAR(255),
    received_at DATETIME NOT NULL,
    CONSTRAINT fk_stockin_medicine
        FOREIGN KEY (medicine_id) REFERENCES medicines (id),
    CONSTRAINT fk_stockin_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers (id)
);
