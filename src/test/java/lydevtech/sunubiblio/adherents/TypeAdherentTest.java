package lydevtech.sunubiblio.adherents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TypeAdherentTest {

    @Test
    void un_etudiant_peut_emprunter_au_plus_3_exemplaires_et_un_enseignant_5() {
        assertEquals(3, TypeAdherent.ETUDIANT.quotaEmprunts());
        assertEquals(5, TypeAdherent.ENSEIGNANT.quotaEmprunts());
    }
}
