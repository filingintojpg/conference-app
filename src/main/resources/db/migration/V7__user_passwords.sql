ALTER TABLE users ADD COLUMN password_hash VARCHAR(100);

-- Общий демонстрационный пароль: password123
UPDATE users SET password_hash = '$2a$10$jleDGHsGwYURrLlTbEj4iexmv8Ovu8nNk1.qAWsG9Ryqh6LtAQTke';

ALTER TABLE users ALTER COLUMN password_hash SET NOT NULL;

UPDATE users SET email_local = lower(email_local), email_domain = lower(email_domain);
