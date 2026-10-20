INSERT INTO users (full_name, email, role) VALUES
    ('Иван Иванов',   'ivan@example.com',  'PARTICIPANT'),
    ('Борис Петров',  'boris@example.com', 'PARTICIPANT'),
    ('Анна Ахматова', 'anna@example.com',  'PARTICIPANT');

INSERT INTO users (full_name, email, role) VALUES
    ('Мария Сидорова', 'maria@example.com', 'ORGANIZER'),
    ('Пётр Смирнов',   'petr@example.com',  'ORGANIZER');

INSERT INTO directions (name, deadline, organizer_id) VALUES
    ('Программирование', now() + INTERVAL '1 year',
        (SELECT id FROM users WHERE email = 'maria@example.com')),
    ('Русский язык',     now() + INTERVAL '1 year',
        (SELECT id FROM users WHERE email = 'maria@example.com')),
    ('Биология',         now() - INTERVAL '1 day',
        (SELECT id FROM users WHERE email = 'petr@example.com'));
