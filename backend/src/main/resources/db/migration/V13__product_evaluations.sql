CREATE TABLE product_evaluations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_eval_user FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_eval_user_product (user_id, product_id)
);
