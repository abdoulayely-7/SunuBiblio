package lydevtech.sunubiblio.emprunts.internal.domain;

import lydevtech.sunubiblio.emprunts.DateRetourInvalideException;
import lydevtech.sunubiblio.emprunts.EmpruntDejaRenduException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmpruntTest {

    private static final LocalDate DEBUT = LocalDate.of(2026, 3, 2);

    private Emprunt unEmprunt() {
        return Emprunt.ouvrir(UUID.randomUUID(), UUID.randomUUID(), DEBUT);
    }

    @Test
    void un_pret_dure_14_jours() {
        assertEquals(DEBUT.plusDays(14), unEmprunt().dateEcheance());
    }

    @Test
    void un_emprunt_neuf_est_en_cours() {
        assertTrue(unEmprunt().enCours());
    }

    @Test
    void rendu_le_jour_de_l_echeance_ou_avant_il_n_y_a_aucun_retard() {
        Emprunt emprunt = unEmprunt();

        assertEquals(0, emprunt.rendu(emprunt.dateEcheance()).joursDeRetard());
        assertEquals(0, emprunt.rendu(DEBUT.plusDays(3)).joursDeRetard());
    }

    @Test
    void rendu_3_jours_apres_l_echeance_il_y_a_3_jours_de_retard() {
        Emprunt emprunt = unEmprunt();

        assertEquals(3, emprunt.rendu(emprunt.dateEcheance().plusDays(3)).joursDeRetard());
    }

    @Test
    void un_emprunt_rendu_n_est_plus_en_cours() {
        assertFalse(unEmprunt().rendu(DEBUT.plusDays(5)).enCours());
    }

    @Test
    void rendre_deux_fois_le_meme_emprunt_est_refuse() {
        Emprunt rendu = unEmprunt().rendu(DEBUT.plusDays(5));

        assertThrows(EmpruntDejaRenduException.class, () -> rendu.rendu(DEBUT.plusDays(6)));
    }

    @Test
    void une_date_de_retour_avant_la_date_d_emprunt_est_refusee() {
        assertThrows(DateRetourInvalideException.class, () -> unEmprunt().rendu(DEBUT.minusDays(1)));
    }
}
