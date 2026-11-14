package lydevtech.sunubiblio.pret.internal.domain;

import lydevtech.sunubiblio.pret.DateRetourInvalideException;
import lydevtech.sunubiblio.pret.EmpruntDejaRestitueException;
import lydevtech.sunubiblio.pret.StatutEmprunt;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmpruntTest {

    private static final LocalDate DEBUT = LocalDate.of(2026, 3, 2);

    private Emprunt unEmprunt() {
        return Emprunt.ouvrir("EMP-1", "ADH-1", "EX-1", DEBUT);
    }

    @Test
    void un_pret_dure_14_jours() {
        assertEquals(DEBUT.plusDays(14), unEmprunt().dateEcheance());
    }

    @Test
    void un_emprunt_neuf_est_en_cours() {
        assertEquals(StatutEmprunt.EN_COURS, unEmprunt().statut());
    }

    @Test
    void restitue_le_jour_de_l_echeance_ou_avant_il_n_y_a_aucun_retard() {
        Emprunt emprunt = unEmprunt();

        assertEquals(0, emprunt.restituer(emprunt.dateEcheance()).joursDeRetard());
        assertEquals(0, emprunt.restituer(DEBUT.plusDays(3)).joursDeRetard());
    }

    @Test
    void restitue_3_jours_apres_l_echeance_il_y_a_3_jours_de_retard() {
        Emprunt emprunt = unEmprunt();

        assertEquals(3, emprunt.restituer(emprunt.dateEcheance().plusDays(3)).joursDeRetard());
    }

    @Test
    void un_emprunt_restitue_change_de_statut() {
        assertEquals(StatutEmprunt.RESTITUE, unEmprunt().restituer(DEBUT.plusDays(5)).statut());
    }

    @Test
    void restituer_deux_fois_le_meme_emprunt_est_refuse() {
        Emprunt restitue = unEmprunt().restituer(DEBUT.plusDays(5));

        assertThrows(EmpruntDejaRestitueException.class, () -> restitue.restituer(DEBUT.plusDays(6)));
    }

    @Test
    void une_date_de_retour_avant_la_date_d_emprunt_est_refusee() {
        assertThrows(DateRetourInvalideException.class, () -> unEmprunt().restituer(DEBUT.minusDays(1)));
    }
}
