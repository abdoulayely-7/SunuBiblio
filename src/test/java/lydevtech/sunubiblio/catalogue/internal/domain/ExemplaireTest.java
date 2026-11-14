package lydevtech.sunubiblio.catalogue.internal.domain;

import lydevtech.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import lydevtech.sunubiblio.catalogue.ExemplaireIndisponibleException;
import lydevtech.sunubiblio.catalogue.StatutExemplaire;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExemplaireTest {

    private Exemplaire unExemplaire() {
        return new Exemplaire("EX-1", "978-2-7087-0000-0");
    }

    @Test
    void un_exemplaire_neuf_est_disponible() {
        assertEquals(StatutExemplaire.DISPONIBLE, unExemplaire().getStatut());
        assertTrue(unExemplaire().isDisponible());
    }

    @Test
    void emprunter_un_exemplaire_disponible_le_marque_emprunte() {
        Exemplaire exemplaire = unExemplaire();

        exemplaire.emprunter();

        assertEquals(StatutExemplaire.EMPRUNTE, exemplaire.getStatut());
    }

    @Test
    void emprunter_un_exemplaire_deja_emprunte_ou_retire_est_refuse() {
        Exemplaire emprunte = unExemplaire();
        emprunte.emprunter();
        Exemplaire retire = unExemplaire();
        retire.retirer();

        assertThrows(ExemplaireIndisponibleException.class, emprunte::emprunter);
        assertThrows(ExemplaireIndisponibleException.class, retire::emprunter);
    }

    @Test
    void rendre_un_exemplaire_emprunte_le_rend_disponible() {
        Exemplaire exemplaire = unExemplaire();
        exemplaire.emprunter();

        exemplaire.rendre();

        assertEquals(StatutExemplaire.DISPONIBLE, exemplaire.getStatut());
    }

    @Test
    void rendre_un_exemplaire_qui_n_est_pas_emprunte_est_refuse() {
        assertThrows(ExemplaireEtatInvalideException.class, unExemplaire()::rendre);
    }

    @Test
    void retirer_un_exemplaire_disponible_le_marque_retire() {
        Exemplaire exemplaire = unExemplaire();

        exemplaire.retirer();

        assertEquals(StatutExemplaire.RETIRE, exemplaire.getStatut());
    }

    @Test
    void retirer_un_exemplaire_en_cours_d_emprunt_est_refuse() {
        Exemplaire exemplaire = unExemplaire();
        exemplaire.emprunter();

        assertThrows(ExemplaireEtatInvalideException.class, exemplaire::retirer);
    }

    @Test
    void un_exemplaire_restaure_garde_son_etat() {
        Exemplaire exemplaire = Exemplaire.restaurer("EX-2", "978-1", StatutExemplaire.EMPRUNTE);

        assertEquals(StatutExemplaire.EMPRUNTE, exemplaire.getStatut());
    }

    @Test
    void un_livre_sans_isbn_ou_sans_titre_est_refuse() {
        assertThrows(IllegalArgumentException.class, () -> new Livre(" ", "Ethiopiques", "Senghor"));
        assertThrows(IllegalArgumentException.class, () -> new Livre("978-1", "", "Senghor"));
    }
}
