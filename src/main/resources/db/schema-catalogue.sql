CREATE TABLE IF NOT EXISTS catalogue_livre (
    id UUID PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS catalogue_exemplaire (
    id UUID PRIMARY KEY,
    livre_id UUID NOT NULL REFERENCES catalogue_livre (id),
    etat VARCHAR(16) NOT NULL
);
