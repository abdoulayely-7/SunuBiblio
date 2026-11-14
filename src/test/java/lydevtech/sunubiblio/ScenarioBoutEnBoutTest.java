package lydevtech.sunubiblio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Scenario du sujet, rejoue de bout en bout par l'API REST :
 * ajouter un livre avec 1 exemplaire, inscrire un etudiant, emprunter, rendre avec 3 jours de retard,
 * constater la penalite de 300 XOF, voir un nouvel emprunt refuse, payer la penalite, emprunter a nouveau.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScenarioBoutEnBoutTest {

    @Autowired
    private MockMvc mvc;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    @Test
    void scenario_du_livre_emprunte_rendu_en_retard_puis_penalite_payee() throws Exception {
        // 1. Ajouter un livre avec 1 exemplaire
        var livre = api.post("/livres",
                "{\"titre\": \"Une si longue lettre\", \"auteur\": \"Mariama Ba\", \"nombreExemplaires\": 1}");
        livre.andExpect(status().isCreated())
                .andExpect(jsonPath("$.exemplairesDisponibles").value(1));
        String livreId = ApiClient.lire(livre, "$.id");
        String exemplaireId = ApiClient.lire(livre, "$.exemplaires[0].id");

        // 2. Inscrire un etudiant
        var adherent = api.post("/adherents", "{\"nom\": \"Awa Diop\", \"type\": \"ETUDIANT\"}");
        adherent.andExpect(status().isCreated());
        String adherentId = ApiClient.lire(adherent, "$.id");

        // 3. Emprunter : le pret dure 14 jours
        var emprunt = api.post("/emprunts",
                "{\"adherentId\": \"" + adherentId + "\", \"exemplaireId\": \"" + exemplaireId + "\"}");
        emprunt.andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateRetour").doesNotExist());
        String empruntId = ApiClient.lire(emprunt, "$.id");
        LocalDate dateEmprunt = LocalDate.parse(ApiClient.<String>lire(emprunt, "$.dateEmprunt"));
        LocalDate dateEcheance = LocalDate.parse(ApiClient.<String>lire(emprunt, "$.dateEcheance"));
        assertEquals(dateEmprunt.plusDays(14), dateEcheance);

        // 4. L'exemplaire n'est plus disponible
        api.get("/livres/" + livreId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exemplairesDisponibles").value(0))
                .andExpect(jsonPath("$.exemplaires[0].etat").value("EMPRUNTE"));
        api.get("/situations/" + adherentId)
                .andExpect(jsonPath("$.empruntsEnCours.length()").value(1))
                .andExpect(jsonPath("$.penalites.length()").value(0));

        // 5. Rendre avec 3 jours de retard : une penalite de 300 XOF existe
        var retour = api.post("/emprunts/" + empruntId + "/retour",
                "{\"dateRetour\": \"" + dateEcheance.plusDays(3) + "\"}");
        retour.andExpect(status().isOk())
                .andExpect(jsonPath("$.joursDeRetard").value(3))
                .andExpect(jsonPath("$.penalite.montantXof").value(300))
                .andExpect(jsonPath("$.penalite.statut").value("IMPAYEE"));
        String penaliteId = ApiClient.lire(retour, "$.penalite.id");

        // 6. L'exemplaire est de nouveau disponible
        api.get("/livres/" + livreId)
                .andExpect(jsonPath("$.exemplairesDisponibles").value(1))
                .andExpect(jsonPath("$.exemplaires[0].etat").value("DISPONIBLE"));
        api.get("/situations/" + adherentId)
                .andExpect(jsonPath("$.empruntsEnCours.length()").value(0))
                .andExpect(jsonPath("$.penalites[0].montantXof").value(300))
                .andExpect(jsonPath("$.penalites[0].statut").value("IMPAYEE"));

        // 7. Un nouvel emprunt est refuse tant que la penalite est impayee
        api.post("/emprunts", "{\"adherentId\": \"" + adherentId + "\", \"exemplaireId\": \"" + exemplaireId + "\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Emprunt refusé"));

        // 8. Payer la penalite
        api.post("/penalites/" + penaliteId + "/paiement")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PAYEE"));

        // 9. L'emprunt est maintenant accepte
        api.post("/emprunts", "{\"adherentId\": \"" + adherentId + "\", \"exemplaireId\": \"" + exemplaireId + "\"}")
                .andExpect(status().isCreated());
        api.get("/situations/" + adherentId)
                .andExpect(jsonPath("$.empruntsEnCours.length()").value(1))
                .andExpect(jsonPath("$.penalites[0].statut").value("PAYEE"));

        // 10. L'adherent a ete prevenu a chaque etape (SMS simule) : 2 emprunts, 1 retour, 1 penalite creee, 1 payee
        api.get("/notifications?adherentId=" + adherentId)
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].message", hasItem(containsString("3 jour(s) de retard"))))
                .andExpect(jsonPath("$[*].message", hasItem(containsString("pénalité de 300 XOF a été créée"))))
                .andExpect(jsonPath("$[*].message", hasItem(containsString("pénalité de 300 XOF est payée"))));
    }
}
