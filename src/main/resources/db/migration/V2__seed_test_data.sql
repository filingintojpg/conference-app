INSERT INTO users (last_name, first_name, middle_name, email_local, email_domain, role) VALUES
    ('Иванов',   'Иван',   'Иванович',     'ivan',   'example.com', 'PARTICIPANT'),
    ('Петров',   'Борис',  'Сергеевич',    'boris',  'example.com', 'PARTICIPANT'),
    ('Ахматова', 'Анна',   'Андреевна',    'anna',   'example.com', 'PARTICIPANT');

INSERT INTO users (last_name, first_name, middle_name, email_local, email_domain, role) VALUES
    ('Сидорова', 'Мария',  'Александровна', 'maria', 'example.com', 'ORGANIZER'),
    ('Смирнов',  'Пётр',   'Николаевич',    'petr',  'example.com', 'ORGANIZER');

INSERT INTO directions (name, deadline, organizer_id) VALUES
    ('Программирование', now() + INTERVAL '1 year',
        (SELECT id FROM users WHERE email_local = 'maria' AND email_domain = 'example.com')),
    ('Русский язык',     now() + INTERVAL '1 year',
        (SELECT id FROM users WHERE email_local = 'maria' AND email_domain = 'example.com')),
    ('Биология',         now() - INTERVAL '1 day',
        (SELECT id FROM users WHERE email_local = 'petr' AND email_domain = 'example.com'));
