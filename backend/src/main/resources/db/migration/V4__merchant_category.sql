ALTER TABLE news ADD COLUMN category VARCHAR(80) NOT NULL DEFAULT '';
ALTER TABLE products ADD COLUMN user_id BIGINT;
ALTER TABLE news ADD COLUMN user_id BIGINT;
UPDATE news SET category = '政策解读' WHERE id = 1;
UPDATE news SET category = '市场行情' WHERE id = 2;
