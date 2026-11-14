CREATE TABLE IF NOT EXISTS emprunts_emprunt (
    id UUID PRIMARY KEY,
    adherent_id UUID NOT NULL,
    exemplaire_id UUID NOT NULL,
    date_emprunt DATE NOT NULL,
    date_echeance DATE NOT NULL,
    date_retour DATE
);

CREATE TABLE IF NOT EXISTS emprunts_blocage (
    penalite_id UUID PRIMARY KEY,
    adherent_id UUID NOT NULL
);
