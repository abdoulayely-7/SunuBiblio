package lydevtech.sunubiblio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContratOpenApiTest {

    private static final Path FICHIER_CONTRAT = Path.of("docs", "openapi.yaml");

    @Autowired
    private MockMvc mvc;

    @Test
    void tous_les_endpoints_du_sujet_sont_decrits_dans_le_contrat() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("SunuBiblio"))
                .andExpect(jsonPath("$.paths['/api/catalogue/livres'].post").exists())
                .andExpect(jsonPath("$.paths['/api/catalogue/livres'].get").exists())
                .andExpect(jsonPath("$.paths['/api/catalogue/livres/{isbn}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/catalogue/exemplaires/{exemplaireId}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/adherents'].post").exists())
                .andExpect(jsonPath("$.paths['/api/adherents/{adherentId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/prets'].post").exists())
                .andExpect(jsonPath("$.paths['/api/prets'].get").exists())
                .andExpect(jsonPath("$.paths['/api/prets/{empruntId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/prets/{empruntId}/restitution'].post").exists())
                .andExpect(jsonPath("$.paths['/api/penalites/{id}/payer'].post").exists())
                .andExpect(jsonPath("$.paths['/api/penalites'].get").exists());
    }

    @Test
    void les_erreurs_sont_decrites_au_format_problem_details() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/prets'].post.responses['409']['$ref']")
                        .value("#/components/responses/Conflit"))
                .andExpect(jsonPath("$.paths['/api/prets'].post.responses['404']['$ref']")
                        .value("#/components/responses/Introuvable"))
                .andExpect(jsonPath("$.paths['/api/prets'].post.responses['400']['$ref']")
                        .value("#/components/responses/RequeteInvalide"))
                .andExpect(jsonPath("$.components.responses.Conflit.content['application/problem+json'].schema['$ref']")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.components.schemas.ProblemDetail.properties.title").exists());
    }

    @Test
    void les_codes_de_succes_decrits_sont_ceux_du_comportement_reel() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/api/catalogue/livres'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/adherents'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/catalogue/exemplaires/{exemplaireId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/prets'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/prets/{empruntId}/restitution'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/penalites/{id}/payer'].post.responses['204']").exists());
    }

    @Test
    void swagger_ui_est_servi() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    /**
     * Le fichier docs/openapi.yaml est le contrat livre : il doit rester identique a celui que
     * l'application publie. Pour le regenerer : ./mvnw test -Dtest=ContratOpenApiTest -Dopenapi.regenerer=true
     */
    @Test
    void le_fichier_docs_openapi_yaml_est_identique_au_contrat_de_l_application() throws Exception {
        String publie = mvc.perform(get("/v3/api-docs.yaml"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        if (System.getProperty("openapi.regenerer") != null) {
            Files.createDirectories(FICHIER_CONTRAT.getParent());
            Files.writeString(FICHIER_CONTRAT, normaliser(publie), StandardCharsets.UTF_8);
        }

        assertEquals(normaliser(publie), normaliser(Files.readString(FICHIER_CONTRAT, StandardCharsets.UTF_8)),
                "docs/openapi.yaml n'est plus a jour : relancer avec -Dopenapi.regenerer=true");
    }

    private static String normaliser(String texte) {
        return texte.replace("\r\n", "\n").stripTrailing() + "\n";
    }
}
