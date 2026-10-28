
CREATE TABLE IF NOT EXISTS garantias.return_status_history (
    id_history BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_request BIGINT NOT NULL,
    previous_status VARCHAR(30) NOT NULL,
    new_status VARCHAR(30) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_return_history_request
        FOREIGN KEY (id_request)
        REFERENCES garantias.return_requests(id_request),

    INDEX idx_return_history_request (id_request)
) ENGINE = InnoDB;
