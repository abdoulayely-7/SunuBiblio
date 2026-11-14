CREATE TABLE IF NOT EXISTS adherent_adherent (
    id VARCHAR(32) PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    type VARCHAR(16) NOT NULL,
    est_bloque_pour_impayes BOOLEAN NOT NULL DEFAULT FALSE,
    montant_impaye BIGINT NOT NULL DEFAULT 0
);
