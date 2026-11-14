package lydevtech.sunubiblio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdherentIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    @Test
    void un_etudiant_s_inscrit_avec_un_quota_de_3_et_sans_blocage() throws Exception {
        api.post("/api/adherents", "{\"nom\": \"Awa Diop\", \"type\": \"ETUDIANT\"}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/adherents/ADH-")))
                .andExpect(jsonPath("$.id", startsWith("ADH-")))
                .andExpect(jsonPath("$.nom").value("Awa Diop"))
                .andExpect(jsonPath("$.type").value("ETUDIANT"))
                .andExpect(jsonPath("$.quotaEmprunts").value(3))
                .andExpect(jsonPath("$.estBloquePourImpayes").value(false))
                .andExpect(jsonPath("$.montantImpaye").value(0));
    }

    @Test
    void un_enseignant_a_un_quota_de_5() throws Exception {
        api.post("/api/adherents", "{\"nom\": \"Pr Fall\", \"type\": \"ENSEIGNANT\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quotaEmprunts").value(5));
    }

    @Test
    void un_adherent_inscrit_peut_etre_consulte() throws Exception {
        String id = api.inscrire("ENSEIGNANT");

        api.get("/api/adherents/" + id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.type").value("ENSEIGNANT"));
    }
}
