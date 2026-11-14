package lydevtech.sunubiblio;

import lydevtech.sunubiblio.catalogue.CatalogueApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Les pannes du serveur : un message utile pour l'utilisateur, jamais de detail interne. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErreursServeurIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CatalogueApi catalogue;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    @Test
    void une_erreur_inattendue_donne_500_avec_un_identifiant_de_suivi_et_sans_detail_interne() throws Exception {
        when(catalogue.rechercherLivres(any())).thenThrow(new IllegalStateException("secret interne de la base"));

        api.get("/api/catalogue/livres")
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Erreur interne"))
                .andExpect(jsonPath("$.detail").value(containsString("identifiant de suivi")))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(content().string(not(containsString("secret interne"))))
                .andExpect(content().string(not(containsString("IllegalStateException"))))
                .andExpect(content().string(not(containsString("lydevtech"))));
    }

    @Test
    void le_numero_de_suivi_du_message_est_celui_de_la_propriete_traceId() throws Exception {
        when(catalogue.rechercherLivres(any())).thenThrow(new IllegalStateException("panne"));

        var reponse = api.get("/api/catalogue/livres").andExpect(status().isInternalServerError());
        String traceId = ApiClient.lire(reponse, "$.traceId");
        String detail = ApiClient.lire(reponse, "$.detail");

        org.junit.jupiter.api.Assertions.assertTrue(detail.contains(traceId), "le detail doit citer le traceId " + traceId);
    }

    @Test
    void une_base_de_donnees_indisponible_donne_503_avec_retry_after() throws Exception {
        when(catalogue.rechercherLivres(any())).thenThrow(
                new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", new SQLException("connexion refusee")));

        api.get("/api/catalogue/livres")
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "5"))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Service indisponible"))
                .andExpect(jsonPath("$.detail").value(containsString("La base de données est momentanément indisponible")))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(content().string(not(containsString("connexion refusee"))))
                .andExpect(content().string(not(containsString("JDBC"))));
    }

    @Test
    void une_violation_de_contrainte_de_la_base_donne_409_et_non_500() throws Exception {
        when(catalogue.rechercherLivres(any())).thenThrow(new DataIntegrityViolationException("duplicate key value violates ..."));

        api.get("/api/catalogue/livres")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflit de données"))
                .andExpect(jsonPath("$.detail").value(containsString("en conflit avec des données existantes")))
                .andExpect(content().string(not(containsString("duplicate key"))));
    }
}
