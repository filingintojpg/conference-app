CREATE TABLE participants (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE
);

CREATE TABLE directions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    edit_deadline TIMESTAMPTZ NOT NULL
);

CREATE TABLE applications (
    id BIGSERIAL PRIMARY KEY,
    participant_id BIGINT NOT NULL REFERENCES participants(id),
    direction_id BIGINT NOT NULL REFERENCES directions(id),
    title VARCHAR(300) NOT NULL,
    abstract_text TEXT NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    withdrawn_at TIMESTAMPTZ,
    CONSTRAINT applications_status_chk CHECK (status IN ('SUBMITTED', 'WITHDRAWN'))
);
