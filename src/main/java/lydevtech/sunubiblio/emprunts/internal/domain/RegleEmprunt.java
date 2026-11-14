package lydevtech.sunubiblio.emprunts.internal.domain;

import lydevtech.sunubiblio.emprunts.PenaliteImpayeeException;
import lydevtech.sunubiblio.emprunts.QuotaEmpruntsAtteintException;

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
