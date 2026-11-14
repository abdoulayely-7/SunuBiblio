package com.sunubiblio.emprunts.internal.domain;

import com.sunubiblio.emprunts.PenaliteImpayeeException;
import com.sunubiblio.emprunts.QuotaEmpruntsAtteintException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegleEmpruntTest {

    private final UUID adherentId = UUID.randomUUID();

    @Test
    void un_adherent_sous_son_quota_et_sans_penalite_peut_emprunter() {
        assertDoesNotThrow(() -> RegleEmprunt.verifier(adherentId, 3, 2, false));
    }

    @Test
    void un_adherent_qui_a_atteint_son_quota_ne_peut_plus_emprunter() {
        assertThrows(QuotaEmpruntsAtteintException.class, () -> RegleEmprunt.verifier(adherentId, 3, 3, false));
        assertThrows(QuotaEmpruntsAtteintException.class, () -> RegleEmprunt.verifier(adherentId, 5, 5, false));
    }

    @Test
    void un_adherent_avec_une_penalite_impayee_ne_peut_pas_emprunter() {
        assertThrows(PenaliteImpayeeException.class, () -> RegleEmprunt.verifier(adherentId, 3, 0, true));
    }

    @Test
    void la_penalite_impayee_est_signalee_avant_le_quota() {
        assertThrows(PenaliteImpayeeException.class, () -> RegleEmprunt.verifier(adherentId, 3, 3, true));
    }
}
