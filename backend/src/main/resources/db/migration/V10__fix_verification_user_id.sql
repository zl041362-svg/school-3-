UPDATE farmer_verifications SET user_id = (SELECT id FROM users WHERE phone = '13600000000') WHERE user_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM users WHERE id = farmer_verifications.user_id);
UPDATE users SET role = 'farmer' WHERE phone = '13600000000' AND role = 'customer';
