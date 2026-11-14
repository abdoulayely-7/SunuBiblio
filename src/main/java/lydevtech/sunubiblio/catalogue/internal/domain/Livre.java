package lydevtech.sunubiblio.catalogue.internal.domain;

import java.util.UUID;

public record Livre(UUID id, String titre, String auteur) {

    public Livre {
        if (titre == null || titre.isBlank()) {
            throw new IllegalArgumentException("Le titre du livre est obligatoire");
        }
        if (auteur == null || auteur.isBlank()) {
            throw new IllegalArgumentException("L'auteur du livre est obligatoire");
        }
    }

    public static Livre nouveau(String titre, String auteur) {
        return new Livre(UUID.randomUUID(), titre, auteur);
    }
}
