package lydevtech.sunubiblio;

import lydevtech.sunubiblio.penalite.PenaliteStatutChangeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Les regles du pret : quota, blocage par penalites (via evenements), retards et restitutions. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RecordApplicationEvents
class PretIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationEvents evenements;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    /** Emprunte un exemplaire et le rend avec le retard demande ; renvoie la restitution (JSON). */
    private org.springframework.test.web.servlet.ResultActions emprunterPuisRendre(String adherent, String exemplaire,
                                                                                  int joursDeRetard) throws Exception {
        String empruntId = ApiClient.lire(api.post("/api/prets", ApiClient.corpsEmprunt(adherent, exemplaire)), "$.id");
        LocalDate echeance = LocalDate.parse(ApiClient.lire(api.get("/api/prets/" + empruntId), "$.dateEcheance"));
        return api.post("/api/prets/" + empruntId + "/restitution",
                "{\"dateRetour\": \"" + echeance.plusDays(joursDeRetard) + "\"}");
    }

    // --- emprunter ---

    @Test
    void un_emprunt_dure_14_jours_et_rend_l_exemplaire_indisponible() throws Exception {
        var livre = api.nouveauLivre(1);
        String adherent = api.inscrire("ETUDIANT");

        api.post("/api/prets", ApiClient.corpsEmprunt(adherent, livre.exemplaires().get(0)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_COURS"))
                .andExpect(jsonPath("$.dateRetour").value(nullValue()))
                .andExpect(jsonPath("$.dateEcheance").value(LocalDate.now().plusDays(14).toString()));

        api.get("/api/catalogue/livres/" + livre.isbn())
                .andExpect(jsonPath("$.exemplaires[0].statut").value("EMPRUNTE"));
    }

    @Test
    void un_etudiant_ne_peut_pas_depasser_3_emprunts_et_un_enseignant_5() throws Exception {
        String etudiant = api.inscrire("ETUDIANT");
        String enseignant = api.inscrire("ENSEIGNANT");
        List<String> exemplaires = api.nouveauLivre(9).exemplaires();

        for (int i = 0; i < 3; i++) {
            api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaires.get(i))).andExpect(status().isCreated());
        }
        api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaires.get(3))).andExpect(status().isConflict());

        for (int i = 4; i < 9; i++) {
            api.post("/api/prets", ApiClient.corpsEmprunt(enseignant, exemplaires.get(i))).andExpect(status().isCreated());
        }
        api.post("/api/prets", ApiClient.corpsEmprunt(enseignant, api.nouveauLivre(1).exemplaires().get(0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("a déjà 5 exemplaires empruntés")));
    }

    @Test
    void rendre_un_exemplaire_libere_une_place_du_quota() throws Exception {
        String etudiant = api.inscrire("ETUDIANT");
        List<String> exemplaires = api.nouveauLivre(4).exemplaires();
        String premier = null;
        for (int i = 0; i < 3; i++) {
            String id = ApiClient.lire(api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaires.get(i))), "$.id");
            premier = premier == null ? id : premier;
        }

        api.post("/api/prets/" + premier + "/restitution").andExpect(status().isOk());

        api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaires.get(3))).andExpect(status().isCreated());
    }

    @Test
    void un_refus_n_a_aucun_effet_de_bord_sur_l_exemplaire() throws Exception {
        String etudiant = api.inscrire("ETUDIANT");
        List<String> exemplaires = api.nouveauLivre(4).exemplaires();
        for (int i = 0; i < 3; i++) {
            api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaires.get(i)));
        }

        api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaires.get(3))).andExpect(status().isConflict());

        api.get("/api/prets?adherentId=" + etudiant).andExpect(jsonPath("$", hasSize(3)));
        org.junit.jupiter.api.Assertions.assertTrue(
                ApiClient.<List<String>>lire(api.get("/api/catalogue/livres"),
                        "$[*].exemplaires[?(@.id=='" + exemplaires.get(3) + "')].statut").contains("DISPONIBLE"));
    }

    @Test
    void les_emprunts_en_cours_n_incluent_pas_les_exemplaires_rendus() throws Exception {
        String adherent = api.inscrire("ETUDIANT");
        List<String> exemplaires = api.nouveauLivre(2).exemplaires();
        String rendu = ApiClient.lire(api.post("/api/prets", ApiClient.corpsEmprunt(adherent, exemplaires.get(0))), "$.id");
        String garde = ApiClient.lire(api.post("/api/prets", ApiClient.corpsEmprunt(adherent, exemplaires.get(1))), "$.id");
        api.post("/api/prets/" + rendu + "/restitution").andExpect(status().isOk());

        api.get("/api/prets?adherentId=" + adherent)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(garde));
    }

    // --- restitutions et penalites ---

    @Test
    void un_retour_a_temps_ne_cree_aucune_penalite() throws Exception {
        String adherent = api.inscrire("ETUDIANT");

        emprunterPuisRendre(adherent, api.nouveauLivre(1).exemplaires().get(0), 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joursDeRetard").value(0))
                .andExpect(jsonPath("$.penalite").value(nullValue()))
                .andExpect(jsonPath("$.emprunt.statut").value("RESTITUE"));

        api.get("/api/penalites?adherentId=" + adherent).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void un_retour_en_retard_cree_une_penalite_de_100_XOF_par_jour_et_bloque_l_adherent() throws Exception {
        String adherent = api.inscrire("ETUDIANT");
        String exemplaire = api.nouveauLivre(1).exemplaires().get(0);

        emprunterPuisRendre(adherent, exemplaire, 3)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joursDeRetard").value(3))
                .andExpect(jsonPath("$.penalite.montant").value(300))
                .andExpect(jsonPath("$.penalite.statut").value("IMPAYEE"))
                .andExpect(jsonPath("$.penalite.exemplaireId").value(exemplaire));

        api.get("/api/adherents/" + adherent)
                .andExpect(jsonPath("$.estBloquePourImpayes").value(true))
                .andExpect(jsonPath("$.montantImpaye").value(300));
        api.post("/api/prets", ApiClient.corpsEmprunt(adherent, api.nouveauLivre(1).exemplaires().get(0)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Emprunt refusé"))
                .andExpect(jsonPath("$.detail").value(containsString("300 XOF de pénalités impayées")));
    }

    @Test
    void la_restitution_sans_corps_utilise_la_date_du_jour() throws Exception {
        String adherent = api.inscrire("ETUDIANT");
        String empruntId = ApiClient.lire(api.post("/api/prets",
                ApiClient.corpsEmprunt(adherent, api.nouveauLivre(1).exemplaires().get(0))), "$.id");

        api.post("/api/prets/" + empruntId + "/restitution")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emprunt.dateRetour").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.joursDeRetard").value(0));
    }

    @Test
    void l_exemplaire_rendu_est_de_nouveau_disponible() throws Exception {
        var livre = api.nouveauLivre(1);
        emprunterPuisRendre(api.inscrire("ETUDIANT"), livre.exemplaires().get(0), 0).andExpect(status().isOk());

        api.get("/api/catalogue/livres/" + livre.isbn()).andExpect(jsonPath("$.exemplaires[0].statut").value("DISPONIBLE"));
    }

    // --- blocage : l'adherent reagit aux evenements de penalite ---

    @Test
    void payer_la_penalite_debloque_l_adherent_et_il_peut_emprunter_de_nouveau() throws Exception {
        String adherent = api.inscrire("ETUDIANT");
        String penaliteId = ApiClient.lire(emprunterPuisRendre(adherent, api.nouveauLivre(1).exemplaires().get(0), 2),
                "$.penalite.id");

        api.post("/api/penalites/" + penaliteId + "/payer").andExpect(status().isNoContent());

        api.get("/api/adherents/" + adherent)
                .andExpect(jsonPath("$.estBloquePourImpayes").value(false))
                .andExpect(jsonPath("$.montantImpaye").value(0));
        api.get("/api/penalites?adherentId=" + adherent).andExpect(jsonPath("$[0].statut").value("REGLEE"));
        api.post("/api/prets", ApiClient.corpsEmprunt(adherent, api.nouveauLivre(1).exemplaires().get(0)))
                .andExpect(status().isCreated());
    }

    @Test
    void avec_deux_penalites_l_adherent_reste_bloque_tant_que_les_deux_ne_sont_pas_reglees() throws Exception {
        String adherent = api.inscrire("ENSEIGNANT");
        // le blocage ne s'applique qu'a l'emprunt suivant : on emprunte les deux exemplaires avant le premier retard
        List<String> exemplaires = api.nouveauLivre(2).exemplaires();
        String emprunt1 = ApiClient.lire(api.post("/api/prets", ApiClient.corpsEmprunt(adherent, exemplaires.get(0))), "$.id");
        String emprunt2 = ApiClient.lire(api.post("/api/prets", ApiClient.corpsEmprunt(adherent, exemplaires.get(1))), "$.id");
        LocalDate echeance = LocalDate.parse(ApiClient.lire(api.get("/api/prets/" + emprunt1), "$.dateEcheance"));
        String penalite1 = ApiClient.lire(api.post("/api/prets/" + emprunt1 + "/restitution",
                "{\"dateRetour\": \"" + echeance.plusDays(1) + "\"}"), "$.penalite.id");
        String penalite2 = ApiClient.lire(api.post("/api/prets/" + emprunt2 + "/restitution",
                "{\"dateRetour\": \"" + echeance.plusDays(2) + "\"}"), "$.penalite.id");

        api.get("/api/adherents/" + adherent)
                .andExpect(jsonPath("$.estBloquePourImpayes").value(true))
                .andExpect(jsonPath("$.montantImpaye").value(300));

        api.post("/api/penalites/" + penalite1 + "/payer").andExpect(status().isNoContent());
        api.get("/api/adherents/" + adherent)
                .andExpect(jsonPath("$.estBloquePourImpayes").value(true))
                .andExpect(jsonPath("$.montantImpaye").value(200));

        api.post("/api/penalites/" + penalite2 + "/payer").andExpect(status().isNoContent());
        api.get("/api/adherents/" + adherent)
                .andExpect(jsonPath("$.estBloquePourImpayes").value(false))
                .andExpect(jsonPath("$.montantImpaye").value(0));
    }

    @Test
    void la_penalite_publie_un_evenement_a_la_creation_et_au_paiement() throws Exception {
        String adherent = api.inscrire("ETUDIANT");
        evenements.clear();

        String penaliteId = ApiClient.lire(emprunterPuisRendre(adherent, api.nouveauLivre(1).exemplaires().get(0), 4),
                "$.penalite.id");
        api.post("/api/penalites/" + penaliteId + "/payer").andExpect(status().isNoContent());

        List<PenaliteStatutChangeEvent> publies = evenements.stream(PenaliteStatutChangeEvent.class)
                .filter(e -> e.adherentId().equals(adherent)).toList();
        assertEquals(2, publies.size());
        assertTrue(publies.get(0).aDesImpayes());
        assertEquals(400, publies.get(0).montantTotalImpaye());
        assertFalse(publies.get(1).aDesImpayes());
        assertEquals(0, publies.get(1).montantTotalImpaye());
    }
}
