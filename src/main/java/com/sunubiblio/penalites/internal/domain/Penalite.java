package com.sunubiblio.penalites.internal.domain;

import com.sunubiblio.penalites.PenaliteDejaPayeeException;
import com.sunubiblio.penalites.StatutPenalite;

import java.util.UUID;

public record Penalite(UUID id, UUID adherentId, UUID empruntId, int joursDeRetard, int montantXof,
                       StatutPenalite statut) {

    /** Tarif de la penalite, defini a cet unique endroit. */
    public static final int TARIF_PAR_JOUR_XOF = 100;

    public static Penalite pourRetard(UUID adherentId, UUID empruntId, int joursDeRetard) {
        if (joursDeRetard < 1) {
            throw new IllegalArgumentException("Une pénalité suppose au moins 1 jour de retard");
        }
        return new Penalite(UUID.randomUUID(), adherentId, empruntId, joursDeRetard,
                joursDeRetard * TARIF_PAR_JOUR_XOF, StatutPenalite.IMPAYEE);
    }

    public Penalite payee() {
        if (statut == StatutPenalite.PAYEE) {
            throw new PenaliteDejaPayeeException(id);
        }
        return new Penalite(id, adherentId, empruntId, joursDeRetard, montantXof, StatutPenalite.PAYEE);
    }
}
