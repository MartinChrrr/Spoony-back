-- RGPD Art. 9: horodatage du consentement explicite au traitement des données de santé.
-- Capturé à l'inscription. Nullable pour les comptes existants antérieurs au correctif.
ALTER TABLE users ADD COLUMN consent_given_at TIMESTAMP;
