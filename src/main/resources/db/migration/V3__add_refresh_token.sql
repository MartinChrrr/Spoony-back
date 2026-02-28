-- V3 : Ajout du stockage du refresh token hashe (SHA-256)
ALTER TABLE users ADD COLUMN refresh_token_hash VARCHAR(255);
