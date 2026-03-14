-- V4 : Ajout de la colonne suggested sur user_task_logs
ALTER TABLE user_task_logs ADD COLUMN suggested BOOLEAN NOT NULL DEFAULT true;
