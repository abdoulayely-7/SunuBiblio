package lydevtech.sunubiblio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Chaque cas d'erreur de l'API : bon code, format Problem Details et message explicite en francais. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErreursApiIntegrationTest {

    private static final String INCONNU = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mvc;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    private static ResultActions auFormatProblemDetails(ResultActions reponse, int statut) throws Exception {
        return reponse.andExpect(status().is(statut))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(statut))
                .andExpect(jsonPath("$.title").isNotEmpty())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    private String adherent() throws Exception {
        return ApiClient.lire(api.post("/adherents", "{\"nom\": \"Adherent\", \"type\": \"ETUDIANT\"}"), "$.id");
    }

    private List<String> exemplaires(int nombre) throws Exception {
        var livre = api.post("/livres", "{\"titre\": \"Livre " + UUID.randomUUID()
                + "\", \"auteur\": \"Auteur\", \"nombreExemplaires\": " + nombre + "}");
        return ApiClient.lire(livre, "$.exemplaires[*].id");
    }

    // --- corps de requete ---

    @Test
    void un_json_mal_forme_est_refuse_avec_une_explication() throws Exception {
        auFormatProblemDetails(api.post("/adherents", "{\"nom\": \"Awa\", "), 400)
                .andExpect(jsonPath("$.detail").value(containsString("n'est pas un JSON valide")));
    }

    @Test
    void un_corps_absent_est_refuse_avec_une_explication() throws Exception {
        auFormatProblemDetails(api.post("/livres", ""), 400)
                .andExpect(jsonPath("$.detail").value(containsString("Le corps de la requête est obligatoire")));
    }

    @Test
    void une_valeur_inconnue_pour_une_enumeration_liste_les_valeurs_possibles() throws Exception {
        auFormatProblemDetails(api.post("/adherents", "{\"nom\": \"Awa\", \"type\": \"VISITEUR\"}"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("Le champ 'type' n'accepte pas la valeur 'VISITEUR'")))
                .andExpect(jsonPath("$.detail").value(containsString("ETUDIANT, ENSEIGNANT")));
    }

    @Test
    void un_nombre_attendu_mais_du_texte_recu_nomme_le_champ() throws Exception {
        auFormatProblemDetails(api.post("/livres",
                "{\"titre\": \"T\", \"auteur\": \"A\", \"nombreExemplaires\": \"beaucoup\"}"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("Le champ 'nombreExemplaires'")))
                .andExpect(jsonPath("$.detail").value(containsString("un nombre entier est attendu")))
                .andExpect(jsonPath("$.detail").value(containsString("'beaucoup'")));
    }

    @Test
    void une_date_mal_formee_precise_le_format_attendu() throws Exception {
        auFormatProblemDetails(api.post("/emprunts/" + INCONNU + "/retour", "{\"dateRetour\": \"16/03/2026\"}"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("Le champ 'dateRetour'")))
                .andExpect(jsonPath("$.detail").value(containsString("AAAA-MM-JJ")));
    }

    // --- validation des champs ---

    @Test
    void tous_les_champs_manquants_sont_listes_un_par_un() throws Exception {
        auFormatProblemDetails(api.post("/livres", "{}"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("3 erreurs")))
                .andExpect(jsonPath("$.erreurs.length()").value(3))
                .andExpect(jsonPath("$.erreurs[*].champ", containsInAnyOrder("titre", "auteur", "nombreExemplaires")))
                .andExpect(jsonPath("$.erreurs[*].message", containsInAnyOrder(
                        "Le titre du livre est obligatoire.",
                        "L'auteur du livre est obligatoire.",
                        "Le nombre d'exemplaires est obligatoire.")));
    }

    @Test
    void une_seule_erreur_de_champ_est_dite_telle_quelle() throws Exception {
        auFormatProblemDetails(api.post("/adherents", "{\"nom\": \"  \", \"type\": \"ETUDIANT\"}"), 400)
                .andExpect(jsonPath("$.detail").value("Le nom de l'adhérent est obligatoire."))
                .andExpect(jsonPath("$.erreurs[0].champ").value("nom"));
    }

    @Test
    void un_titre_trop_long_est_refuse_au_lieu_de_provoquer_une_erreur_de_base() throws Exception {
        String titreTropLong = "x".repeat(300);

        auFormatProblemDetails(api.post("/livres",
                "{\"titre\": \"" + titreTropLong + "\", \"auteur\": \"A\", \"nombreExemplaires\": 1}"), 400)
                .andExpect(jsonPath("$.detail").value("Le titre du livre ne doit pas dépasser 255 caractères."));
    }

    @Test
    void le_nombre_d_exemplaires_doit_rester_dans_les_bornes() throws Exception {
        auFormatProblemDetails(api.post("/livres", "{\"titre\": \"T\", \"auteur\": \"A\", \"nombreExemplaires\": 0}"), 400)
                .andExpect(jsonPath("$.detail").value("Un livre doit avoir au moins 1 exemplaire."));
        auFormatProblemDetails(api.post("/livres", "{\"titre\": \"T\", \"auteur\": \"A\", \"nombreExemplaires\": 501}"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("plus de 500 exemplaires")));
    }

    @Test
    void un_emprunt_sans_identifiants_dit_lesquels_manquent() throws Exception {
        auFormatProblemDetails(api.post("/emprunts", "{}"), 400)
                .andExpect(jsonPath("$.erreurs[*].message", containsInAnyOrder(
                        "L'identifiant de l'adhérent (adherentId) est obligatoire.",
                        "L'identifiant de l'exemplaire (exemplaireId) est obligatoire.")));
    }

    @Test
    void un_adherent_sans_type_rappelle_les_types_possibles() throws Exception {
        auFormatProblemDetails(api.post("/adherents", "{\"nom\": \"Awa\"}"), 400)
                .andExpect(jsonPath("$.detail").value("Le type d'adhérent est obligatoire : ETUDIANT ou ENSEIGNANT."));
    }

    // --- adresse de la requete ---

    @Test
    void un_identifiant_mal_forme_dans_l_adresse_donne_un_exemple_valide() throws Exception {
        auFormatProblemDetails(api.get("/livres/abc"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("Le paramètre 'livreId'")))
                .andExpect(jsonPath("$.detail").value(containsString("identifiant UUID")))
                .andExpect(jsonPath("$.detail").value(containsString("'abc'")));
        auFormatProblemDetails(api.delete("/exemplaires/12345"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("Le paramètre 'exemplaireId'")));
    }

    @Test
    void une_route_inconnue_donne_404_avec_la_route_demandee() throws Exception {
        auFormatProblemDetails(api.get("/inexistant"), 404)
                .andExpect(jsonPath("$.title").value("Ressource introuvable"))
                .andExpect(jsonPath("$.detail").value(containsString("GET /inexistant")))
                .andExpect(jsonPath("$.detail").value(containsString("/swagger-ui.html")));
    }

    @Test
    void une_methode_non_autorisee_donne_405_avec_les_methodes_possibles() throws Exception {
        auFormatProblemDetails(mvc.perform(MockMvcRequestBuilders.put("/livres")), 405)
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.detail").value(containsString("La méthode PUT n'est pas autorisée")))
                .andExpect(jsonPath("$.detail").value(containsString("GET")))
                .andExpect(jsonPath("$.detail").value(containsString("POST")));
    }

    @Test
    void un_type_de_contenu_non_json_donne_415_avec_le_format_attendu() throws Exception {
        auFormatProblemDetails(mvc.perform(MockMvcRequestBuilders.post("/livres")
                .contentType(MediaType.TEXT_PLAIN).content("titre=x")), 415)
                .andExpect(jsonPath("$.detail").value(containsString("'text/plain'")))
                .andExpect(jsonPath("$.detail").value(containsString("application/json")));
    }

    // --- regles du metier : le message dit quoi faire ---

    @Test
    void un_adherent_ou_un_exemplaire_inconnu_est_nomme_dans_le_message() throws Exception {
        String exemplaire = exemplaires(1).get(0);

        auFormatProblemDetails(api.post("/emprunts", "{\"adherentId\": \"" + INCONNU + "\", \"exemplaireId\": \"" + exemplaire + "\"}"), 404)
                .andExpect(jsonPath("$.detail").value(containsString("Aucun adhérent ne correspond à l'identifiant " + INCONNU)));
        auFormatProblemDetails(api.post("/emprunts", "{\"adherentId\": \"" + adherent() + "\", \"exemplaireId\": \"" + INCONNU + "\"}"), 404)
                .andExpect(jsonPath("$.detail").value(containsString("Aucun exemplaire ne correspond à l'identifiant " + INCONNU)));
        auFormatProblemDetails(api.get("/livres/" + INCONNU), 404)
                .andExpect(jsonPath("$.detail").value(containsString("Aucun livre ne correspond")));
        auFormatProblemDetails(api.post("/penalites/" + INCONNU + "/paiement"), 404)
                .andExpect(jsonPath("$.detail").value(containsString("Aucune pénalité ne correspond")));
    }

    @Test
    void un_exemplaire_deja_emprunte_ou_retire_explique_pourquoi_il_est_refuse() throws Exception {
        List<String> exemplaires = exemplaires(2);
        String corpsEmprunte = "{\"adherentId\": \"" + adherent() + "\", \"exemplaireId\": \"" + exemplaires.get(0) + "\"}";
        api.post("/emprunts", corpsEmprunte).andExpect(status().isCreated());
        api.delete("/exemplaires/" + exemplaires.get(1));

        auFormatProblemDetails(api.post("/emprunts", "{\"adherentId\": \"" + adherent() + "\", \"exemplaireId\": \"" + exemplaires.get(0) + "\"}"), 409)
                .andExpect(jsonPath("$.detail").value(containsString("il est déjà emprunté")));
        auFormatProblemDetails(api.post("/emprunts", "{\"adherentId\": \"" + adherent() + "\", \"exemplaireId\": \"" + exemplaires.get(1) + "\"}"), 409)
                .andExpect(jsonPath("$.detail").value(containsString("il a été retiré du catalogue")));
        auFormatProblemDetails(api.delete("/exemplaires/" + exemplaires.get(0)), 409)
                .andExpect(jsonPath("$.detail").value(containsString("doit d'abord être rendu")));
    }

    @Test
    void une_date_de_retour_trop_ancienne_cite_les_deux_dates() throws Exception {
        String empruntId = ApiClient.lire(api.post("/emprunts", "{\"adherentId\": \"" + adherent()
                + "\", \"exemplaireId\": \"" + exemplaires(1).get(0) + "\"}"), "$.id");

        auFormatProblemDetails(api.post("/emprunts/" + empruntId + "/retour", "{\"dateRetour\": \"2000-01-01\"}"), 400)
                .andExpect(jsonPath("$.detail").value(containsString("La date de retour (2000-01-01) ne peut pas précéder la date d'emprunt")));
    }

    @Test
    void un_quota_atteint_et_un_double_retour_disent_quoi_faire() throws Exception {
        String etudiant = adherent();
        List<String> exemplaires = exemplaires(4);
        String premierEmprunt = null;
        for (int i = 0; i < 3; i++) {
            var emprunt = api.post("/emprunts", "{\"adherentId\": \"" + etudiant + "\", \"exemplaireId\": \"" + exemplaires.get(i) + "\"}");
            if (i == 0) {
                premierEmprunt = ApiClient.lire(emprunt, "$.id");
            }
        }

        auFormatProblemDetails(api.post("/emprunts", "{\"adherentId\": \"" + etudiant + "\", \"exemplaireId\": \"" + exemplaires.get(3) + "\"}"), 409)
                .andExpect(jsonPath("$.title").value("Emprunt refusé"))
                .andExpect(jsonPath("$.detail").value(containsString("a déjà 3 exemplaires empruntés")))
                .andExpect(jsonPath("$.detail").value(containsString("il doit en rendre un")));

        api.post("/emprunts/" + premierEmprunt + "/retour").andExpect(status().isOk());
        auFormatProblemDetails(api.post("/emprunts/" + premierEmprunt + "/retour"), 409)
                .andExpect(jsonPath("$.detail").value(containsString("a déjà été rendu")));
    }

    @Test
    void les_erreurs_ne_montrent_jamais_de_detail_interne() throws Exception {
        String corps = api.get("/livres/abc").andReturn().getResponse().getContentAsString();

        org.hamcrest.MatcherAssert.assertThat(corps, not(containsString("Exception")));
        org.hamcrest.MatcherAssert.assertThat(corps, not(containsString("lydevtech")));
        org.hamcrest.MatcherAssert.assertThat(corps, not(containsString("at org.springframework")));
    }
}
