package com.sunubiblio.emprunts.internal.domain;

import com.sunubiblio.emprunts.PenaliteImpayeeException;
import com.sunubiblio.emprunts.QuotaEmpruntsAtteintException;

import java.util.UUID;

/** Regles qui decident si un adherent a le droit d'emprunter un exemplaire de plus. */
public final class RegleEmprunt {

    private RegleEmprunt() {
    }

    public static void verifier(UUID adherentId, int quotaMax, long empruntsEnCours, boolean penaliteImpayee) {
        if (penaliteImpayee) {
            throw new PenaliteImpayeeException(adherentId);
        }
        if (empruntsEnCours >= quotaMax) {
            throw new QuotaEmpruntsAtteintException(adherentId, quotaMax);
        }
    }
}
