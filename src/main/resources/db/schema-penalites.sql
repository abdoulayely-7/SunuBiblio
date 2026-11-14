CREATE TABLE IF NOT EXISTS penalites_penalite (
    id UUID PRIMARY KEY,
    adherent_id UUID NOT NULL,
    emprunt_id UUID NOT NULL,
    jours_de_retard INTEGER NOT NULL,
    montant_xof INTEGER NOT NULL,
    statut VARCHAR(16) NOT NULL,
    cree_le TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
