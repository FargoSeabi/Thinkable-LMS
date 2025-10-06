-- Add password hashing support to users table
-- This migration adds the password_hashed column to track which passwords have been hashed

ALTER TABLE users ADD COLUMN password_hashed BOOLEAN DEFAULT FALSE;

-- Create index for faster migration queries
CREATE INDEX idx_users_password_hashed ON users(password_hashed);

-- Update any existing users that might already have BCrypt hashed passwords
-- (BCrypt hashes start with $2a$, $2b$, or $2y$ and are 60 characters long)
UPDATE users
SET password_hashed = TRUE
WHERE password REGEXP '^\\$2[ayb]\\$.{56}$';

-- Add comment for documentation
COMMENT ON COLUMN users.password_hashed IS 'Indicates whether the password is hashed with BCrypt (true) or stored as plain text (false)';