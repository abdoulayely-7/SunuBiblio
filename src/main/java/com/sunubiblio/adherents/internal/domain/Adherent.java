package com.sunubiblio.adherents.internal.domain;

import com.sunubiblio.adherents.TypeAdherent;

import java.util.UUID;

public record Adherent(UUID id, String nom, TypeAdherent type) {

    public Adherent {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'adhérent est obligatoire");
        }
        if (type == null) {
            throw new IllegalArgumentException("Le type de l'adhérent est obligatoire (ETUDIANT ou ENSEIGNANT)");
        }
    }

    public static Adherent nouveau(String nom, TypeAdherent type) {
        return new Adherent(UUID.randomUUID(), nom, type);
    }
}
