-- Add username column to users table
ALTER TABLE users ADD COLUMN username VARCHAR(50) UNIQUE;

-- Add otp_code column to email_verifications table
ALTER TABLE email_verifications ADD COLUMN otp_code VARCHAR(6);

-- Create index for faster username lookup
CREATE INDEX idx_users_username ON users(username);

-- Create index for email_verifications lookup
CREATE INDEX idx_email_verifications_email ON email_verifications(email);
CREATE INDEX idx_email_verifications_created_at ON email_verifications(created_at);
