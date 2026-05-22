DELETE FROM product_evaluations WHERE id > 0;

INSERT INTO product_evaluations(product_id, user_id, rating, content, created_at, updated_at)
SELECT 1, id, 5, '颗粒饱满，煮饭很香，家里人都爱吃，会继续回购。', '2026-05-10 10:30:00', '2026-05-10 10:30:00' FROM users WHERE phone = '13900000000';

INSERT INTO product_evaluations(product_id, user_id, rating, content, created_at, updated_at)
SELECT 3, id, 5, '酸甜适中，孩子特别爱吃，已经是第三次买了。', '2026-05-11 09:15:00', '2026-05-11 09:15:00' FROM users WHERE phone = '13900000000';

INSERT INTO product_evaluations(product_id, user_id, rating, content, created_at, updated_at)
SELECT 4, id, 5, '汁多味甜，个头也大，会回购！', '2026-05-14 11:30:00', '2026-05-14 11:30:00' FROM users WHERE phone = '13900000000';

INSERT INTO product_evaluations(product_id, user_id, rating, content, created_at, updated_at)
SELECT 5, id, 5, '陈香醇厚，回甘明显，老树料确实不一样。', '2026-05-10 15:20:00', '2026-05-10 15:20:00' FROM users WHERE phone = '13800000000';

INSERT INTO product_evaluations(product_id, user_id, rating, content, created_at, updated_at)
SELECT 6, id, 4, '颗粒大，免洗即食很方便，每天泡水喝。', '2026-05-11 13:00:00', '2026-05-11 13:00:00' FROM users WHERE phone = '13800000000';

INSERT INTO product_evaluations(product_id, user_id, rating, content, created_at, updated_at)
SELECT 8, id, 5, '脆甜水分足，个头均匀色泽好，值得买。', '2026-05-13 12:00:00', '2026-05-13 12:00:00' FROM users WHERE phone = '13800000000';
