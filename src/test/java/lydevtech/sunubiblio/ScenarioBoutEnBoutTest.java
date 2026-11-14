package lydevtech.sunubiblio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Le scenario du sujet, de bout en bout par l'API : un livre a un exemplaire, un etudiant l'emprunte,
 * le rend avec 3 jours de retard, est bloque par sa penalite de 300 XOF, la paie, puis peut emprunter de nouveau.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScenarioBoutEnBoutTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void le_cycle_complet_d_un_pret_avec_retard_penalite_blocage_et_paiement() throws Exception {
        ApiClient api = new ApiClient(mvc);
        var livre = api.nouveauLivre(1);
        String exemplaire = livre.exemplaires().get(0);
        String etudiant = api.inscrire("ETUDIANT");

        // 1. emprunt : l'exemplaire devient indisponible et un second adherent ne peut pas le prendre
        String empruntId = ApiClient.lire(api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaire)), "$.id");
        api.get("/api/catalogue/livres/" + livre.isbn()).andExpect(jsonPath("$.exemplaires[0].statut").value("EMPRUNTE"));
        api.post("/api/prets", ApiClient.corpsEmprunt(api.inscrire("ETUDIANT"), exemplaire))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("il est déjà emprunté")));
        api.get("/api/prets?adherentId=" + etudiant).andExpect(jsonPath("$", hasSize(1)));

        // 2. restitution 3 jours apres l'echeance : penalite de 3 x 100 = 300 XOF
        LocalDate echeance = LocalDate.parse(ApiClient.lire(api.get("/api/prets/" + empruntId), "$.dateEcheance"));
        var restitution = api.post("/api/prets/" + empruntId + "/restitution",
                        "{\"dateRetour\": \"" + echeance.plusDays(3) + "\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.joursDeRetard").value(3))
                .andExpect(jsonPath("$.penalite.montant").value(300))
                .andExpect(jsonPath("$.penalite.statut").value("IMPAYEE"));
        String penaliteId = ApiClient.lire(restitution, "$.penalite.id");
        api.get("/api/catalogue/livres/" + livre.isbn()).andExpect(jsonPath("$.exemplaires[0].statut").value("DISPONIBLE"));
        api.get("/api/prets?adherentId=" + etudiant).andExpect(jsonPath("$", hasSize(0)));

        // 3. l'evenement de penalite bloque l'adherent : le reemprunt est refuse
        api.get("/api/adherents/" + etudiant)
                .andExpect(jsonPath("$.estBloquePourImpayes").value(true))
                .andExpect(jsonPath("$.montantImpaye").value(300));
        api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaire))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(containsString("300 XOF de pénalités impayées")));
        api.get("/api/penalites?adherentId=" + etudiant)
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(penaliteId));

        // 4. paiement : un second evenement libere l'adherent
        api.post("/api/penalites/" + penaliteId + "/payer").andExpect(status().isNoContent());
        api.get("/api/adherents/" + etudiant).andExpect(jsonPath("$.estBloquePourImpayes").value(false));

        // 5. le reemprunt est maintenant accepte
        api.post("/api/prets", ApiClient.corpsEmprunt(etudiant, exemplaire))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_COURS"));
    }
}
