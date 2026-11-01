ALTER TABLE users DROP CONSTRAINT users_role_chk;
ALTER TABLE users
    ADD CONSTRAINT users_role_chk CHECK (role IN ('PARTICIPANT', 'ORGANIZER', 'EXPERT'));

INSERT INTO users (last_name, first_name, middle_name, email_local, email_domain, role) VALUES
    ('Кузнецов', 'Алексей', 'Викторович',  'alexey', 'example.com', 'EXPERT'),
    ('Орлова',   'Елена',   'Дмитриевна',  'elena',  'example.com', 'EXPERT');
