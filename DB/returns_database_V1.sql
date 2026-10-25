CREATE DATABASE IF NOT EXISTS garantias;

-- Tabla Devoluciones Solicitadas
CREATE TABLE IF NOT EXISTS garantias.return_requests (
    id_request BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,   -- ---- FK
    user_id BIGINT NOT NULL, -- ----- FK
    status VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE', -- PENDIENTE, RECIBIDO, APROBADO, RECHAZADO
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    -- FOREIGN KEY (user_id) REFERENCES ecommerce.users(id_user),
    -- FOREIGN KEY (order_id) REFERENCES ecommerce.orders(id)
) ENGINE = InnoDB;


-- Tabla Productos a Devolver
CREATE TABLE IF NOT EXISTS garantias.return_items (
    id_r_item BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_request BIGINT NOT NULL, -- ---- FK
    item_id INT NOT NULL, -- ---- FK
    item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    reason VARCHAR(50) NOT NULL, -- ITEM_DEFECTUOSO, ITEM_EQUIVOCADO, NO_COMO_DESCRITO, CAMBIO_OPINIÓN, OTRO
    FOREIGN KEY (id_request) REFERENCES garantias.return_requests(id_request)
    -- FOREIGN KEY (product_id) REFERENCES ecommerce.products(product_id)
) ENGINE = InnoDB;


