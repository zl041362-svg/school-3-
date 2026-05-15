INSERT INTO users(phone, password_hash, name, role, status) VALUES
('13800000000', '$2a$10$s2bXgYd3M6YphJ4uvXnfmubj6ibSvQqf0KVkx.JQYm.u06D3hRHfu', 'Admin User', 'admin', 'active'),
('13900000000', '$2a$10$s2bXgYd3M6YphJ4uvXnfmubj6ibSvQqf0KVkx.JQYm.u06D3hRHfu', 'Customer User', 'customer', 'active');
INSERT INTO products(name, region, category, price, stock, summary, description, status) VALUES
('Wuchang Rice', 'Heilongjiang', 'Grain', 28.00, 120, 'New season rice supply', 'Direct sourcing from farm base', 'online'),
('Anxi Tieguanyin', 'Fujian', 'Tea', 66.00, 0, 'Spring tea batch', 'Contains origin certificate details', 'online'),
('Gannan Orange', 'Jiangxi', 'Fruit', 39.00, 64, 'Seasonal fruit in box', 'Cold-chain shipping supported', 'online');
INSERT INTO news(title, author, status, summary, content, published_at) VALUES
('Spring Farming Progress', 'Platform News Team', 'published', 'Updates on spring farming regions', 'Detailed market and farming updates', CURRENT_TIMESTAMP),
('Weekly Price Watch', 'Operations Center', 'published', 'Key category price trends', 'Weekly market analysis and advice', CURRENT_TIMESTAMP);
