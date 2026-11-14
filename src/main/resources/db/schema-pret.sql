CREATE TABLE IF NOT EXISTS pret_emprunt (
    id VARCHAR(32) PRIMARY KEY,
    adherent_id VARCHAR(32) NOT NULL,
    exemplaire_id VARCHAR(32) NOT NULL,
    date_emprunt DATE NOT NULL,
    date_echeance DATE NOT NULL,
    date_retour DATE
);
