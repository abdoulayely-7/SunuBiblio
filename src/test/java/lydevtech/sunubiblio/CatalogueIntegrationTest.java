package lydevtech.sunubiblio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogueIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    @Test
    void F1_ajouter_un_livre_cree_ses_exemplaires_disponibles() throws Exception {
        var reponse = api.post("/livres", """
                {"titre": "Une si longue lettre", "auteur": "Mariama Ba", "nombreExemplaires": 2}
                """);

        reponse.andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.titre").value("Une si longue lettre"))
                .andExpect(jsonPath("$.exemplairesDisponibles").value(2))
                .andExpect(jsonPath("$.exemplaires.length()").value(2));
    }

    @Test
    void F1_ajouter_un_exemplaire_a_un_livre_existant() throws Exception {
        String livreId = ApiClient.lire(api.post("/livres",
                "{\"titre\": \"Le Ventre de l'Atlantique\", \"auteur\": \"Fatou Diome\", \"nombreExemplaires\": 1}"), "$.id");

        api.post("/livres/" + livreId + "/exemplaires")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.etat").value("DISPONIBLE"));

        api.get("/livres/" + livreId).andExpect(jsonPath("$.exemplairesDisponibles").value(2));
    }

    @Test
    void F2_retirer_un_exemplaire_abime_le_rend_indisponible() throws Exception {
        var livre = api.post("/livres",
                "{\"titre\": \"Xala\", \"auteur\": \"Ousmane Sembene\", \"nombreExemplaires\": 2}");
        String livreId = ApiClient.lire(livre, "$.id");
        String exemplaireId = ApiClient.lire(livre, "$.exemplaires[0].id");

        api.delete("/exemplaires/" + exemplaireId).andExpect(status().isNoContent());

        api.get("/livres/" + livreId)
                .andExpect(jsonPath("$.exemplairesDisponibles").value(1))
                .andExpect(jsonPath("$.exemplaires.length()").value(2));
    }

    @Test
    void F4_rechercher_un_livre_par_titre_ou_auteur_montre_les_exemplaires_disponibles() throws Exception {
        api.post("/livres", "{\"titre\": \"Ethiopiques\", \"auteur\": \"Leopold Sedar Senghor\", \"nombreExemplaires\": 3}");

        api.get("/livres?q=senghor")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titre").value("Ethiopiques"))
                .andExpect(jsonPath("$[0].exemplairesDisponibles").value(3));
        api.get("/livres?q=ethiopiq").andExpect(jsonPath("$[0].auteur").value("Leopold Sedar Senghor"));
        api.get("/livres?q=zzz-introuvable").andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void un_livre_inconnu_donne_404_au_format_problem_details() throws Exception {
        api.get("/livres/00000000-0000-0000-0000-000000000000")
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Ressource introuvable"));
    }

    @Test
    void un_livre_sans_titre_est_refuse_avec_400() throws Exception {
        api.post("/livres", "{\"titre\": \"\", \"auteur\": \"Anonyme\", \"nombreExemplaires\": 1}")
                .andExpect(status().isBadRequest());
    }
}
