-- Run this manually once your schema exists (after the app has started at least once
-- with ddl-auto=update, so the `users` table is created).
--
-- Password hash below is BCrypt for the plaintext password: Admin@123
-- Generate your own for production using any BCrypt generator / the PasswordEncoder bean.

INSERT INTO users (name, email, password, phone, role, enabled, created_at)
VALUES (
  'Super Admin',
  '[email protected]',
  '$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5c1XI8FOqXqZQKmt3E3syDzgbf6ry',
  '9999999999',
  'ADMIN',
  true,
  NOW()
);
