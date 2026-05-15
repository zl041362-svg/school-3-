ALTER TABLE products ADD COLUMN farmer VARCHAR(120) NOT NULL DEFAULT '';
UPDATE products SET farmer = '黑土地农场' WHERE id = 1;
UPDATE products SET farmer = '闽茶基地' WHERE id = 2;
UPDATE products SET farmer = '赣南果园合作社' WHERE id = 3;

CREATE TABLE addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    is_default INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    qty INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id)
);

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
    receiver VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    logistics VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    qty INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE farmer_verifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    farmer VARCHAR(120) NOT NULL,
    contact VARCHAR(20) NOT NULL,
    real_name VARCHAR(100) NOT NULL,
    id_number VARCHAR(40) NOT NULL,
    business_no VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    product VARCHAR(120) NOT NULL,
    farmer VARCHAR(120) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE news_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    news_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role VARCHAR(40) NOT NULL UNIQUE,
    members INT NOT NULL DEFAULT 0,
    description VARCHAR(255) NOT NULL
);

CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    role VARCHAR(40) NOT NULL
);

CREATE TABLE operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    operator VARCHAR(40) NOT NULL,
    action VARCHAR(80) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users(phone, password_hash, name, role, status) VALUES
('13600000000', '$2a$10$s2bXgYd3M6YphJ4uvXnfmubj6ibSvQqf0KVkx.JQYm.u06D3hRHfu', 'Merchant User', 'customer', 'active');

INSERT INTO addresses(user_id, receiver, phone, address, is_default) VALUES
(2, '张三', '13800000000', '黑龙江省哈尔滨市道里区中央大街 88 号', 1),
(2, '李四', '13900000000', '江西省赣州市章贡区红旗大道 66 号', 0);

INSERT INTO cart_items(user_id, product_id, qty) VALUES
(2, 1, 2),
(2, 3, 1);

INSERT INTO farmer_verifications(user_id, farmer, contact, real_name, id_number, business_no, status, reason) VALUES
(3, '闽茶基地', '13600000000', '王海', '350123198801011234', 'MZ-2026-001', 'pending', NULL);

INSERT INTO product_reviews(product_id, product, farmer, price, status, reason) VALUES
(2, '安溪铁观音', '闽茶基地', 66.00, 'pending', NULL);

INSERT INTO news_reviews(news_id, title, author, status, reason) VALUES
(1, '春耕进度播报', '平台农讯组', 'pending', NULL);

INSERT INTO roles(role, members, description) VALUES
('admin', 1, '平台全局管理与配置权限'),
('auditor', 1, '负责商品、资讯、认证审核');

INSERT INTO permissions(module, action, role) VALUES
('product-review', 'approve', 'auditor'),
('news-review', 'reject', 'auditor'),
('rbac', 'manage', 'admin');

INSERT INTO operation_logs(operator, action, detail) VALUES
('admin', 'seed_data', '初始化后台演示数据'),
('auditor', 'seed_data', '初始化审核演示数据');

