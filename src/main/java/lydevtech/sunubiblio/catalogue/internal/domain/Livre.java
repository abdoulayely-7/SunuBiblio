package lydevtech.sunubiblio.catalogue.internal.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Livre {

    private final String isbn;
    private final String titre;
    private final String auteur;
    private final List<Exemplaire> exemplaires = new ArrayList<>();

    public Livre(String isbn, String titre, String auteur) {
        if (isbn == null || isbn.isBlank()) {
            throw new IllegalArgumentException("L'ISBN ne peut pas être vide.");
        }
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide.");
        }
        this.isbn = isbn;
        this.titre = titre;
        this.auteur = auteur;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitre() {
        return titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public List<Exemplaire> getExemplaires() {
        return Collections.unmodifiableList(exemplaires);
    }

    public Exemplaire ajouterExemplaire(String exemplaireId) {
        Exemplaire exemplaire = new Exemplaire(exemplaireId, this.isbn);
        this.exemplaires.add(exemplaire);
        return exemplaire;
    }

    public Optional<Exemplaire> trouverExemplaire(String exemplaireId) {
        return exemplaires.stream()
                .filter(e -> e.getId().equals(exemplaireId))
                .findFirst();
    }
}

