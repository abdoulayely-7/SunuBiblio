package lydevtech.sunubiblio.penalite.internal.domain;

import lydevtech.sunubiblio.penalite.PenaliteDejaRegleeException;
import lydevtech.sunubiblio.penalite.StatutPenalite;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PenaliteTest {

    @Test
    void la_penalite_est_de_100_XOF_par_jour_de_retard() {
        assertEquals(300, new Penalite("PEN-1", "ADH-1", "EX-1", 3).getMontant());
        assertEquals(100, new Penalite("PEN-2", "ADH-1", "EX-1", 1).getMontant());
        assertEquals(1_400, new Penalite("PEN-3", "ADH-1", "EX-1", 14).getMontant());
    }

    @Test
    void une_penalite_nouvelle_est_impayee() {
        Penalite penalite = new Penalite("PEN-1", "ADH-1", "EX-1", 2);

        assertEquals(StatutPenalite.IMPAYEE, penalite.getStatut());
        assertTrue(penalite.isImpayee());
    }

    @Test
    void sans_jour_de_retard_il_n_y_a_pas_de_penalite() {
        assertThrows(IllegalArgumentException.class, () -> new Penalite("PEN-1", "ADH-1", "EX-1", 0));
        assertThrows(IllegalArgumentException.class, () -> new Penalite("PEN-1", "ADH-1", "EX-1", -2));
    }

    @Test
    void regler_une_penalite_la_marque_reglee_sans_changer_son_montant() {
        Penalite penalite = new Penalite("PEN-1", "ADH-1", "EX-1", 3);

        penalite.regler();

        assertEquals(StatutPenalite.REGLEE, penalite.getStatut());
        assertEquals(300, penalite.getMontant());
    }

    @Test
    void regler_deux_fois_la_meme_penalite_est_refuse() {
        Penalite penalite = new Penalite("PEN-1", "ADH-1", "EX-1", 3);
        penalite.regler();

        assertThrows(PenaliteDejaRegleeException.class, penalite::regler);
    }
}
