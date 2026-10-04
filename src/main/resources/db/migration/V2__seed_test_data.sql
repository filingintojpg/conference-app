INSERT INTO participants (full_name, email) VALUES
    ('Иван Иванов', 'ivan@example.com'),
    ('Борис Петров', 'boris@example.com'),
    ('Анна Ахматова', 'anna@example.com');

INSERT INTO directions (name, deadline) VALUES
    ('Программирование', now() + INTERVAL '1 year'),
    ('Русский язык',     now() + INTERVAL '1 year'),
    ('Биология',         now() - INTERVAL '1 day');
