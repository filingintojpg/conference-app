ALTER TABLE directions
    ADD COLUMN expert_id BIGINT UNIQUE,
    ADD COLUMN expert_role VARCHAR(30) NOT NULL DEFAULT 'EXPERT',
    ADD CONSTRAINT directions_expert_role_chk CHECK (expert_role = 'EXPERT'),
    ADD CONSTRAINT directions_expert_fk FOREIGN KEY (expert_id, expert_role) REFERENCES users (id, role);

UPDATE directions SET expert_id = (SELECT id FROM users WHERE email_local = 'alexey' AND email_domain = 'example.com')
WHERE name = 'Программирование';

UPDATE directions SET expert_id = (SELECT id FROM users WHERE email_local = 'elena' AND email_domain = 'example.com')
WHERE name = 'Русский язык';
