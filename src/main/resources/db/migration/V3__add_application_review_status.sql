ALTER TABLE applications
    ADD COLUMN review_status VARCHAR(30) NOT NULL DEFAULT 'PENDING';

ALTER TABLE applications
    ADD CONSTRAINT applications_review_status_chk CHECK (review_status IN ('PENDING', 'APPROVED', 'REJECTED'));
