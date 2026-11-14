package com.sunubiblio.emprunts;

import java.util.UUID;

/** Evenement annonce par le module emprunts quand un exemplaire est rendu. */
public record ExemplaireRendu(UUID empruntId, UUID adherentId, UUID exemplaireId, int joursDeRetard) {
}
