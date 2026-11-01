ALTER TABLE applications
    ADD COLUMN transfer_from_direction_id BIGINT REFERENCES directions(id),
    ADD COLUMN transfer_to_direction_id BIGINT REFERENCES directions(id),
    ADD CONSTRAINT applications_transfer_chk CHECK (
        transfer_to_direction_id IS NULL
        OR (transfer_from_direction_id IS NOT NULL AND transfer_to_direction_id <> transfer_from_direction_id)
    );

CREATE INDEX applications_transfer_to_idx ON applications (transfer_to_direction_id);
