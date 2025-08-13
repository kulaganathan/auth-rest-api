-- PostgreSQL setup script for Auth Server
-- Run this script as a PostgreSQL superuser (e.g., postgres)

-- Create database
CREATE DATABASE auth_db;

-- Create user (optional - you can use existing postgres user)
-- CREATE USER auth_user WITH PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;
-- GRANT ALL PRIVILEGES ON DATABASE auth_db TO auth_user;

-- Connect to the auth_db database
\c auth_db;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Note: The actual table creation will be handled by Flyway migrations
-- This script just sets up the database environment

-- Verify database creation
SELECT current_database();
