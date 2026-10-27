CREATE DATABASE IF NOT EXISTS garantias;
-- Cabecera del reclamo de garantía
CREATE TABLE IF NOT EXISTS garantias.warranty_claims (
    id_claim BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,   -- FK pendiente: ecommerce.orders(id)
    user_id BIGINT NOT NULL,    -- FK pendiente: ecommerce.users(id_user)
    status VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_warranty_claims_status
    CHECK (status IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    INDEX idx_warranty_claims_user (user_id),
    INDEX idx_warranty_claims_order (order_id)
    ) ENGINE = InnoDB;

-- Productos incluidos en cada reclamo
CREATE TABLE IF NOT EXISTS garantias.warranty_items (
    id_w_item BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_claim BIGINT NOT NULL,
    item_id BIGINT NOT NULL,            -- FK pendiente: tabla de productos
    item_name VARCHAR(100) NOT NULL,    -- snapshot del nombre al reclamar
    quantity INT NOT NULL,
    reason VARCHAR(50) NOT NULL,        -- enum WarrantyReason.java
    description VARCHAR(500) NULL,      -- texto libre del cliente
    purchase_date DATETIME NOT NULL,
    warranty_expires_at DATETIME NOT NULL,
    CONSTRAINT fk_warranty_items_claim
    FOREIGN KEY (id_claim) REFERENCES garantias.warranty_claims (id_claim)
    ON DELETE CASCADE,
    CONSTRAINT chk_warranty_items_quantity CHECK (quantity > 0),
    INDEX idx_warranty_items_item (item_id)
    ) ENGINE = InnoDB;