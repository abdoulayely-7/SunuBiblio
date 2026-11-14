package com.sunubiblio.catalogue.internal.domain;

import com.sunubiblio.catalogue.EtatExemplaire;
import com.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import com.sunubiblio.catalogue.ExemplaireIndisponibleException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExemplaireTest {

    private final UUID livreId = UUID.randomUUID();

    @Test
    void un_exemplaire_neuf_est_disponible() {
        assertEquals(EtatExemplaire.DISPONIBLE, Exemplaire.neuf(livreId).etat());
    }

    @Test
    void emprunter_un_exemplaire_disponible_le_marque_emprunte() {
        assertEquals(EtatExemplaire.EMPRUNTE, Exemplaire.neuf(livreId).emprunte().etat());
    }

    @Test
    void emprunter_un_exemplaire_deja_emprunte_est_refuse() {
        Exemplaire emprunte = Exemplaire.neuf(livreId).emprunte();

        assertThrows(ExemplaireIndisponibleException.class, emprunte::emprunte);
    }

    @Test
    void emprunter_un_exemplaire_retire_est_refuse() {
        Exemplaire retire = Exemplaire.neuf(livreId).retire();

        assertThrows(ExemplaireIndisponibleException.class, retire::emprunte);
    }

    @Test
    void rendre_un_exemplaire_emprunte_le_rend_disponible() {
        Exemplaire rendu = Exemplaire.neuf(livreId).emprunte().rendu();

        assertEquals(EtatExemplaire.DISPONIBLE, rendu.etat());
    }

    @Test
    void rendre_un_exemplaire_qui_n_est_pas_emprunte_est_refuse() {
        Exemplaire disponible = Exemplaire.neuf(livreId);

        assertThrows(ExemplaireEtatInvalideException.class, disponible::rendu);
    }

    @Test
    void retirer_un_exemplaire_disponible_le_marque_retire() {
        assertEquals(EtatExemplaire.RETIRE, Exemplaire.neuf(livreId).retire().etat());
    }

    @Test
    void retirer_un_exemplaire_en_cours_d_emprunt_est_refuse() {
        Exemplaire emprunte = Exemplaire.neuf(livreId).emprunte();

        assertThrows(ExemplaireEtatInvalideException.class, emprunte::retire);
    }

    @Test
    void un_livre_sans_titre_ou_sans_auteur_est_refuse() {
        assertThrows(IllegalArgumentException.class, () -> Livre.nouveau(" ", "Senghor"));
        assertThrows(IllegalArgumentException.class, () -> Livre.nouveau("Ethiopiques", ""));
    }
}
