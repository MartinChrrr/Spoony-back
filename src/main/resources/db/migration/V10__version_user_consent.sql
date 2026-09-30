-- Preserve evidence of the exact legal texts accepted by each user.
-- Existing rows must not be associated retroactively with the current texts.
ALTER TABLE users
    ADD COLUMN consent_version VARCHAR(64) NOT NULL DEFAULT 'legacy-unversioned';

ALTER TABLE users
    ADD COLUMN privacy_policy_version VARCHAR(64) NOT NULL DEFAULT 'legacy-unversioned';

ALTER TABLE users
    ADD CONSTRAINT chk_users_consent_version_not_blank
        CHECK (LENGTH(TRIM(consent_version)) > 0);

ALTER TABLE users
    ADD CONSTRAINT chk_users_privacy_policy_version_not_blank
        CHECK (LENGTH(TRIM(privacy_policy_version)) > 0);
