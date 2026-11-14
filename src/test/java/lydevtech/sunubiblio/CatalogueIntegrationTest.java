package lydevtech.sunubiblio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
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
    void ajouter_un_livre_cree_ses_exemplaires_tous_disponibles() throws Exception {
        String isbn = "978-" + UUID.randomUUID().toString().substring(0, 8);

        api.post("/api/catalogue/livres", "{\"isbn\": \"" + isbn
                        + "\", \"titre\": \"Une si longue lettre\", \"auteur\": \"Mariama Ba\", \"nombreExemplaires\": 3}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/catalogue/livres/" + isbn))
                .andExpect(jsonPath("$.isbn").value(isbn))
                .andExpect(jsonPath("$.titre").value("Une si longue lettre"))
                .andExpect(jsonPath("$.exemplaires", hasSize(3)))
                .andExpect(jsonPath("$.exemplaires[*].statut", everyDisponible()))
                .andExpect(jsonPath("$.exemplaires[0].id", startsWith("EX-")));
    }

    private static org.hamcrest.Matcher<Iterable<? extends String>> everyDisponible() {
        return org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.equalTo("DISPONIBLE"));
    }

    @Test
    void ajouter_un_isbn_deja_au_catalogue_ajoute_des_exemplaires_a_ce_livre() throws Exception {
        var livre = api.nouveauLivre(1);

        api.post("/api/catalogue/livres", "{\"isbn\": \"" + livre.isbn()
                        + "\", \"titre\": \"Autre titre\", \"auteur\": \"Autre\", \"nombreExemplaires\": 2}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exemplaires", hasSize(3)))
                .andExpect(jsonPath("$.titre").value("Livre " + livre.isbn()));
    }

    @Test
    void consulter_un_livre_montre_l_etat_de_ses_exemplaires() throws Exception {
        var livre = api.nouveauLivre(2);
        api.post("/api/prets", ApiClient.corpsEmprunt(api.inscrire("ETUDIANT"), livre.exemplaires().get(0)));

        api.get("/api/catalogue/livres/" + livre.isbn())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exemplaires", hasSize(2)))
                .andExpect(jsonPath("$.exemplaires[?(@.id=='" + livre.exemplaires().get(0) + "')].statut").value(hasItem("EMPRUNTE")))
                .andExpect(jsonPath("$.exemplaires[?(@.id=='" + livre.exemplaires().get(1) + "')].statut").value(hasItem("DISPONIBLE")));
    }

    @Test
    void retirer_un_exemplaire_le_marque_retire_mais_garde_sa_trace() throws Exception {
        var livre = api.nouveauLivre(2);

        api.delete("/api/catalogue/exemplaires/" + livre.exemplaires().get(0)).andExpect(status().isNoContent());

        api.get("/api/catalogue/livres/" + livre.isbn())
                .andExpect(jsonPath("$.exemplaires", hasSize(2)))
                .andExpect(jsonPath("$.exemplaires[?(@.id=='" + livre.exemplaires().get(0) + "')].statut").value(hasItem("RETIRE")));
    }

    // --- recherche (F4) ---

    @Test
    void la_recherche_trouve_par_titre_auteur_ou_isbn_sans_tenir_compte_des_majuscules() throws Exception {
        String marque = UUID.randomUUID().toString().substring(0, 6);
        String isbn = "978-" + marque;
        api.post("/api/catalogue/livres", "{\"isbn\": \"" + isbn + "\", \"titre\": \"Xalaat " + marque
                + "\", \"auteur\": \"Cheikh Hamidou Kane\", \"nombreExemplaires\": 1}").andExpect(status().isCreated());

        api.get("/api/catalogue/livres?q=XALAAT " + marque.toUpperCase())
                .andExpect(jsonPath("$[*].isbn", hasItem(isbn)));
        api.get("/api/catalogue/livres?q=hamidou")
                .andExpect(jsonPath("$[*].isbn", hasItem(isbn)));
        api.get("/api/catalogue/livres?q=" + isbn)
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void une_recherche_sans_resultat_donne_une_liste_vide_et_non_une_erreur() throws Exception {
        api.get("/api/catalogue/livres?q=" + UUID.randomUUID())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void une_recherche_vide_ou_sans_texte_liste_tout_le_catalogue() throws Exception {
        var livre = api.nouveauLivre(1);

        api.get("/api/catalogue/livres").andExpect(jsonPath("$[*].isbn", hasItem(livre.isbn())));
        api.get("/api/catalogue/livres?q=").andExpect(jsonPath("$[*].isbn", hasItem(livre.isbn())));
        api.get("/api/catalogue/livres?q=   ").andExpect(jsonPath("$[*].isbn", hasItem(livre.isbn())));
    }

    @Test
    void les_caracteres_speciaux_de_sql_sont_traites_comme_du_texte() throws Exception {
        var livre = api.nouveauLivre(1);

        api.get("/api/catalogue/livres?q=%")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].isbn", not(hasItem(livre.isbn()))));
        api.get("/api/catalogue/livres?q=_")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].isbn", not(hasItem(livre.isbn()))));
        api.get("/api/catalogue/livres?q=' OR '1'='1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void le_catalogue_liste_les_exemplaires_de_chaque_livre() throws Exception {
        List<String> exemplaires = api.nouveauLivre(2).exemplaires();

        api.get("/api/catalogue/livres").andExpect(jsonPath("$[*].exemplaires[*].id", hasItem(exemplaires.get(1))));
    }
}
