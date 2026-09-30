-- Normalize the login identifier once so the application-level normalization and
-- the database unique constraint enforce the same rule.
UPDATE users SET email = LOWER(TRIM(email));

ALTER TABLE user_tasks
    ADD CONSTRAINT ck_user_tasks_importance
    CHECK (importance IN ('LOW', 'MEDIUM', 'HIGH'));

ALTER TABLE user_tasks
    ADD CONSTRAINT ck_user_tasks_status
    CHECK (status IN ('ACTIVE', 'COMPLETED', 'ARCHIVED'));

ALTER TABLE user_task_logs
    ADD CONSTRAINT ck_user_task_logs_status
    CHECK (status IN ('PLANNED', 'COMPLETED', 'SKIPPED'));

CREATE INDEX idx_user_task_logs_user_date
    ON user_task_logs (user_id, date);

CREATE INDEX idx_user_task_logs_user_status_date
    ON user_task_logs (user_id, status, date);

CREATE INDEX idx_user_tasks_user_status_due_date
    ON user_tasks (user_id, status, due_date);
