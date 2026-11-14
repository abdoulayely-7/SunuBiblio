package lydevtech.sunubiblio.penalites.internal.domain;

import lydevtech.sunubiblio.penalites.PenaliteDejaPayeeException;
import lydevtech.sunubiblio.penalites.StatutPenalite;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PenaliteTest {

    private final UUID adherentId = UUID.randomUUID();
    private final UUID empruntId = UUID.randomUUID();

    @Test
    void la_penalite_est_de_100_XOF_par_jour_de_retard() {
        assertEquals(300, Penalite.pourRetard(adherentId, empruntId, 3).montantXof());
        assertEquals(100, Penalite.pourRetard(adherentId, empruntId, 1).montantXof());
        assertEquals(1_400, Penalite.pourRetard(adherentId, empruntId, 14).montantXof());
    }

    @Test
    void une_penalite_nouvelle_est_impayee() {
        assertEquals(StatutPenalite.IMPAYEE, Penalite.pourRetard(adherentId, empruntId, 2).statut());
    }

    @Test
    void sans_jour_de_retard_il_n_y_a_pas_de_penalite() {
        assertThrows(IllegalArgumentException.class, () -> Penalite.pourRetard(adherentId, empruntId, 0));
        assertThrows(IllegalArgumentException.class, () -> Penalite.pourRetard(adherentId, empruntId, -2));
    }

    @Test
    void payer_une_penalite_la_marque_payee_sans_changer_son_montant() {
        Penalite payee = Penalite.pourRetard(adherentId, empruntId, 3).payee();

        assertEquals(StatutPenalite.PAYEE, payee.statut());
        assertEquals(300, payee.montantXof());
    }

    @Test
    void payer_deux_fois_la_meme_penalite_est_refuse() {
        Penalite payee = Penalite.pourRetard(adherentId, empruntId, 3).payee();

        assertThrows(PenaliteDejaPayeeException.class, payee::payee);
    }
}
