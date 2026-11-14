package com.sunubiblio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpruntsReglesIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    private String inscrire(String type) throws Exception {
        return ApiClient.lire(api.post("/adherents", "{\"nom\": \"Adherent\", \"type\": \"" + type + "\"}"), "$.id");
    }

    private List<String> exemplairesDUnNouveauLivre(int nombre) throws Exception {
        var livre = api.post("/livres", "{\"titre\": \"Livre " + UUID.randomUUID()
                + "\", \"auteur\": \"Auteur\", \"nombreExemplaires\": " + nombre + "}");
        return ApiClient.lire(livre, "$.exemplaires[*].id");
    }

    private String corpsEmprunt(String adherentId, String exemplaireId) {
        return "{\"adherentId\": \"" + adherentId + "\", \"exemplaireId\": \"" + exemplaireId + "\"}";
    }

    @Test
    void un_etudiant_emprunte_au_plus_3_exemplaires_a_la_fois() throws Exception {
        String etudiant = inscrire("ETUDIANT");
        List<String> exemplaires = exemplairesDUnNouveauLivre(4);

        for (int i = 0; i < 3; i++) {
            api.post("/emprunts", corpsEmprunt(etudiant, exemplaires.get(i))).andExpect(status().isCreated());
        }

        api.post("/emprunts", corpsEmprunt(etudiant, exemplaires.get(3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Emprunt refusé"));
    }

    @Test
    void un_enseignant_emprunte_au_plus_5_exemplaires_a_la_fois() throws Exception {
        String enseignant = inscrire("ENSEIGNANT");
        List<String> exemplaires = exemplairesDUnNouveauLivre(6);

        for (int i = 0; i < 5; i++) {
            api.post("/emprunts", corpsEmprunt(enseignant, exemplaires.get(i))).andExpect(status().isCreated());
        }

        api.post("/emprunts", corpsEmprunt(enseignant, exemplaires.get(5))).andExpect(status().isConflict());
    }

    @Test
    void rendre_un_exemplaire_libere_une_place_dans_le_quota() throws Exception {
        String etudiant = inscrire("ETUDIANT");
        List<String> exemplaires = exemplairesDUnNouveauLivre(4);
        String premierEmprunt = null;
        for (int i = 0; i < 3; i++) {
            var emprunt = api.post("/emprunts", corpsEmprunt(etudiant, exemplaires.get(i)));
            if (i == 0) {
                premierEmprunt = ApiClient.lire(emprunt, "$.id");
            }
        }

        api.post("/emprunts/" + premierEmprunt + "/retour").andExpect(status().isOk());

        api.post("/emprunts", corpsEmprunt(etudiant, exemplaires.get(3))).andExpect(status().isCreated());
    }

    @Test
    void un_exemplaire_deja_emprunte_ne_peut_pas_etre_emprunte_par_un_autre() throws Exception {
        List<String> exemplaires = exemplairesDUnNouveauLivre(1);
        api.post("/emprunts", corpsEmprunt(inscrire("ETUDIANT"), exemplaires.get(0))).andExpect(status().isCreated());

        api.post("/emprunts", corpsEmprunt(inscrire("ETUDIANT"), exemplaires.get(0)))
                .andExpect(status().isConflict());
    }

    @Test
    void un_exemplaire_retire_ne_peut_pas_etre_emprunte() throws Exception {
        List<String> exemplaires = exemplairesDUnNouveauLivre(2);
        api.delete("/exemplaires/" + exemplaires.get(0)).andExpect(status().isNoContent());

        api.post("/emprunts", corpsEmprunt(inscrire("ETUDIANT"), exemplaires.get(0)))
                .andExpect(status().isConflict());
    }

    @Test
    void un_exemplaire_en_cours_d_emprunt_ne_peut_pas_etre_retire() throws Exception {
        List<String> exemplaires = exemplairesDUnNouveauLivre(1);
        api.post("/emprunts", corpsEmprunt(inscrire("ETUDIANT"), exemplaires.get(0))).andExpect(status().isCreated());

        api.delete("/exemplaires/" + exemplaires.get(0)).andExpect(status().isConflict());
    }

    @Test
    void un_retour_a_temps_ne_cree_aucune_penalite() throws Exception {
        String etudiant = inscrire("ETUDIANT");
        String exemplaire = exemplairesDUnNouveauLivre(1).get(0);
        String empruntId = ApiClient.lire(api.post("/emprunts", corpsEmprunt(etudiant, exemplaire)), "$.id");

        api.post("/emprunts/" + empruntId + "/retour")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joursDeRetard").value(0))
                .andExpect(jsonPath("$.penalite").doesNotExist());

        api.get("/situations/" + etudiant).andExpect(jsonPath("$.penalites.length()").value(0));
    }

    @Test
    void rendre_deux_fois_le_meme_emprunt_donne_409() throws Exception {
        String exemplaire = exemplairesDUnNouveauLivre(1).get(0);
        String empruntId = ApiClient.lire(api.post("/emprunts", corpsEmprunt(inscrire("ETUDIANT"), exemplaire)), "$.id");

        api.post("/emprunts/" + empruntId + "/retour").andExpect(status().isOk());
        api.post("/emprunts/" + empruntId + "/retour").andExpect(status().isConflict());
    }

    @Test
    void une_date_de_retour_avant_l_emprunt_donne_400() throws Exception {
        String exemplaire = exemplairesDUnNouveauLivre(1).get(0);
        String empruntId = ApiClient.lire(api.post("/emprunts", corpsEmprunt(inscrire("ETUDIANT"), exemplaire)), "$.id");

        api.post("/emprunts/" + empruntId + "/retour", "{\"dateRetour\": \"2000-01-01\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void un_adherent_ou_un_exemplaire_inconnu_donne_404() throws Exception {
        String exemplaire = exemplairesDUnNouveauLivre(1).get(0);
        String inconnu = "00000000-0000-0000-0000-000000000000";

        api.post("/emprunts", corpsEmprunt(inconnu, exemplaire)).andExpect(status().isNotFound());
        api.post("/emprunts", corpsEmprunt(inscrire("ETUDIANT"), inconnu)).andExpect(status().isNotFound());
        api.get("/situations/" + inconnu).andExpect(status().isNotFound());
    }

    @Test
    void une_requete_d_emprunt_incomplete_donne_400() throws Exception {
        api.post("/emprunts", "{\"adherentId\": \"" + UUID.randomUUID() + "\"}").andExpect(status().isBadRequest());
    }
}
