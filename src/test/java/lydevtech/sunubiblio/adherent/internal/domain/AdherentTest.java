package lydevtech.sunubiblio.adherent.internal.domain;

import lydevtech.sunubiblio.adherent.AdherentBloqueException;
import lydevtech.sunubiblio.adherent.QuotaEmpruntsAtteintException;
import lydevtech.sunubiblio.adherent.TypeAdherent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdherentTest {

    private Adherent etudiant() {
        return new Adherent("ADH-1", "Awa Diop", TypeAdherent.ETUDIANT);
    }

    @Test
    void un_etudiant_peut_emprunter_au_plus_3_exemplaires_et_un_enseignant_5() {
        assertEquals(3, TypeAdherent.ETUDIANT.quotaEmprunts());
        assertEquals(5, TypeAdherent.ENSEIGNANT.quotaEmprunts());
    }

    @Test
    void un_adherent_sous_son_quota_et_non_bloque_peut_emprunter() {
        assertDoesNotThrow(() -> etudiant().verifierEligibiliteEmprunt(2));
    }

    @Test
    void un_adherent_qui_a_atteint_son_quota_ne_peut_plus_emprunter() {
        assertThrows(QuotaEmpruntsAtteintException.class, () -> etudiant().verifierEligibiliteEmprunt(3));
        Adherent enseignant = new Adherent("ADH-2", "Pr Fall", TypeAdherent.ENSEIGNANT);
        assertThrows(QuotaEmpruntsAtteintException.class, () -> enseignant.verifierEligibiliteEmprunt(5));
        assertDoesNotThrow(() -> enseignant.verifierEligibiliteEmprunt(4));
    }

    @Test
    void un_adherent_bloque_pour_impayes_ne_peut_pas_emprunter() {
        Adherent adherent = etudiant();
        adherent.mettreAJourImpayes(true, 300);

        assertTrue(adherent.isEstBloquePourImpayes());
        assertThrows(AdherentBloqueException.class, () -> adherent.verifierEligibiliteEmprunt(0));
    }

    @Test
    void le_blocage_est_signale_avant_le_quota() {
        Adherent adherent = etudiant();
        adherent.mettreAJourImpayes(true, 300);

        assertThrows(AdherentBloqueException.class, () -> adherent.verifierEligibiliteEmprunt(3));
    }

    @Test
    void une_fois_les_penalites_reglees_l_adherent_n_est_plus_bloque() {
        Adherent adherent = etudiant();
        adherent.mettreAJourImpayes(true, 300);

        adherent.mettreAJourImpayes(false, 0);

        assertFalse(adherent.isEstBloquePourImpayes());
        assertEquals(0, adherent.getMontantImpaye());
        assertDoesNotThrow(() -> adherent.verifierEligibiliteEmprunt(0));
    }

    @Test
    void un_adherent_sans_nom_ou_sans_type_est_refuse() {
        assertThrows(IllegalArgumentException.class, () -> new Adherent("ADH-1", " ", TypeAdherent.ETUDIANT));
        assertThrows(IllegalArgumentException.class, () -> new Adherent("ADH-1", "Awa", null));
    }
}
