CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,
    CONSTRAINT users_role_chk CHECK (role IN ('PARTICIPANT', 'ORGANIZER')),
    CONSTRAINT users_id_role_uq UNIQUE (id, role)
);

CREATE TABLE directions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    deadline TIMESTAMPTZ NOT NULL,
    organizer_id BIGINT NOT NULL,
    organizer_role VARCHAR(30) NOT NULL DEFAULT 'ORGANIZER',
    CONSTRAINT directions_organizer_role_chk CHECK (organizer_role = 'ORGANIZER'),
    CONSTRAINT directions_organizer_fk FOREIGN KEY (organizer_id, organizer_role) REFERENCES users (id, role)
);

CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES users(id),
    direction_id BIGINT NOT NULL REFERENCES directions(id),
    title VARCHAR(300) NOT NULL,
    abstract_text TEXT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT applications_status_chk CHECK (status IN ('SUBMITTED', 'EDITED', 'WITHDRAWN'))
);
