CREATE DATABASE IF NOT EXISTS OAuthTest;

CREATE TABLE users(
    userId VARCHAR(100),
    email varchar(50) NOT NULL UNIQUE,
    passwordHash varchar(255) NULL,
    authProvider enum('LOCAL', 'GOOGLE') NOT NULL,
    providerId varchar(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (userId)
)