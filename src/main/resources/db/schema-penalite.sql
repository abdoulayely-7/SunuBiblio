CREATE TABLE IF NOT EXISTS penalite_penalite (
    id VARCHAR(32) PRIMARY KEY,
    adherent_id VARCHAR(32) NOT NULL,
    exemplaire_id VARCHAR(32),
    jours_de_retard BIGINT NOT NULL,
    montant BIGINT NOT NULL,
    statut VARCHAR(16) NOT NULL,
    date_creation TIMESTAMP NOT NULL
);
