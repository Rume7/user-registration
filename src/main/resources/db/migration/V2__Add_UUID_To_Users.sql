-- Migration: Add UUID column to users table
-- This migration adds a UUID column to the users table and backfills existing users with UUIDs

-- Add UUID column to users table
ALTER TABLE users ADD COLUMN uuid UUID;

-- Create unique index on UUID column
CREATE UNIQUE INDEX users_uuid_idx ON users(uuid);

-- Backfill existing users with UUIDs
-- This uses PostgreSQL's gen_random_uuid() function
UPDATE users SET uuid = gen_random_uuid() WHERE uuid IS NULL;

-- Make UUID column NOT NULL after backfilling
ALTER TABLE users ALTER COLUMN uuid SET NOT NULL;

-- Add constraint to prevent future NULL UUIDs
ALTER TABLE users ADD CONSTRAINT users_uuid_not_null CHECK (uuid IS NOT NULL); 