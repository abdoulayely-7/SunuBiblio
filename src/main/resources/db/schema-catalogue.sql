CREATE TABLE IF NOT EXISTS catalogue_livre (
    isbn VARCHAR(32) PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS catalogue_exemplaire (
    id VARCHAR(32) PRIMARY KEY,
    livre_isbn VARCHAR(32) NOT NULL REFERENCES catalogue_livre (isbn),
    statut VARCHAR(16) NOT NULL
);
