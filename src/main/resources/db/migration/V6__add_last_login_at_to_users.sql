ALTER TABLE users ADD COLUMN last_login_at TIMESTAMP;
UPDATE users SET last_login_at = updated_at WHERE last_login_at IS NULL;
CREATE INDEX idx_users_last_login_at ON users (last_login_at);
