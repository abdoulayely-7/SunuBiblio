package com.sunubiblio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdherentsIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    @Test
    void F3_inscrire_un_etudiant_lui_donne_un_quota_de_3() throws Exception {
        api.post("/adherents", "{\"nom\": \"Awa Diop\", \"type\": \"ETUDIANT\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("ETUDIANT"))
                .andExpect(jsonPath("$.quotaEmprunts").value(3));
    }

    @Test
    void F3_inscrire_un_enseignant_lui_donne_un_quota_de_5() throws Exception {
        String id = ApiClient.lire(api.post("/adherents", "{\"nom\": \"Pr Fall\", \"type\": \"ENSEIGNANT\"}"), "$.id");

        api.get("/adherents/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotaEmprunts").value(5));
    }

    @Test
    void un_adherent_sans_nom_ou_avec_un_type_inconnu_est_refuse_avec_400() throws Exception {
        api.post("/adherents", "{\"nom\": \"\", \"type\": \"ETUDIANT\"}").andExpect(status().isBadRequest());
        api.post("/adherents", "{\"nom\": \"Awa\", \"type\": \"VISITEUR\"}").andExpect(status().isBadRequest());
        api.post("/adherents", "{\"nom\": \"Awa\"}").andExpect(status().isBadRequest());
    }

    @Test
    void un_adherent_inconnu_donne_404() throws Exception {
        api.get("/adherents/00000000-0000-0000-0000-000000000000").andExpect(status().isNotFound());
    }
}
