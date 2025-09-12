CREATE DATABASE IF NOT EXISTS OAuthTest;

CREATE TABLE users(
    userId VARCHAR(100) DEFAULT (UUID()),
    email varchar(50) NOT NULL UNIQUE,
    password varchar(255) NULL,
    name varchar(100) NULL,
    authProvider enum('LOCAL', 'GOOGLE', 'GITHUB') NOT NULL,
    providerId varchar(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (userId)
);
