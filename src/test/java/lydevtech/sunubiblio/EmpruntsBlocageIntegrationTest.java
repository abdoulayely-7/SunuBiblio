package lydevtech.sunubiblio;

import lydevtech.sunubiblio.penalites.PenaliteCreee;
import lydevtech.sunubiblio.penalites.PenalitePayee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Le pret decide de bloquer un adherent a partir des seuls evenements annonces par le module penalites. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmpruntsBlocageIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ApplicationEventPublisher evenements;

    @Autowired
    private PlatformTransactionManager transactions;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    private String inscrire() throws Exception {
        return ApiClient.lire(api.post("/adherents", "{\"nom\": \"Adherent\", \"type\": \"ETUDIANT\"}"), "$.id");
    }

    private List<String> exemplaires(int nombre) throws Exception {
        var livre = api.post("/livres", "{\"titre\": \"Livre " + UUID.randomUUID()
                + "\", \"auteur\": \"Auteur\", \"nombreExemplaires\": " + nombre + "}");
        return ApiClient.lire(livre, "$.exemplaires[*].id");
    }

    private String corps(String adherentId, String exemplaireId) {
        return "{\"adherentId\": \"" + adherentId + "\", \"exemplaireId\": \"" + exemplaireId + "\"}";
    }

    @Test
    void l_evenement_PenaliteCreee_bloque_l_adherent_et_PenalitePayee_le_debloque() throws Exception {
        String adherent = inscrire();
        List<String> exemplaires = exemplaires(2);
        UUID penaliteId = UUID.randomUUID();

        evenements.publishEvent(new PenaliteCreee(penaliteId, UUID.fromString(adherent), 300));
        api.post("/emprunts", corps(adherent, exemplaires.get(0))).andExpect(status().isConflict());

        evenements.publishEvent(new PenalitePayee(penaliteId, UUID.fromString(adherent), 300));
        api.post("/emprunts", corps(adherent, exemplaires.get(0))).andExpect(status().isCreated());
    }

    @Test
    void avec_deux_penalites_impayees_il_faut_les_payer_toutes_les_deux() throws Exception {
        String adherent = inscrire();
        List<String> exemplaires = exemplaires(1);
        UUID premiere = UUID.randomUUID();
        UUID seconde = UUID.randomUUID();
        evenements.publishEvent(new PenaliteCreee(premiere, UUID.fromString(adherent), 100));
        evenements.publishEvent(new PenaliteCreee(seconde, UUID.fromString(adherent), 200));

        evenements.publishEvent(new PenalitePayee(premiere, UUID.fromString(adherent), 100));
        api.post("/emprunts", corps(adherent, exemplaires.get(0))).andExpect(status().isConflict());

        evenements.publishEvent(new PenalitePayee(seconde, UUID.fromString(adherent), 200));
        api.post("/emprunts", corps(adherent, exemplaires.get(0))).andExpect(status().isCreated());
    }

    @Test
    void le_blocage_d_un_adherent_ne_touche_pas_les_autres() throws Exception {
        String bloque = inscrire();
        String libre = inscrire();
        List<String> exemplaires = exemplaires(2);

        evenements.publishEvent(new PenaliteCreee(UUID.randomUUID(), UUID.fromString(bloque), 100));

        api.post("/emprunts", corps(bloque, exemplaires.get(0))).andExpect(status().isConflict());
        api.post("/emprunts", corps(libre, exemplaires.get(0))).andExpect(status().isCreated());
    }

    @Test
    void un_adherent_bloque_peut_toujours_rendre_ses_exemplaires() throws Exception {
        String adherent = inscrire();
        String exemplaire = exemplaires(1).get(0);
        String empruntId = ApiClient.lire(api.post("/emprunts", corps(adherent, exemplaire)), "$.id");

        evenements.publishEvent(new PenaliteCreee(UUID.randomUUID(), UUID.fromString(adherent), 100));

        api.post("/emprunts/" + empruntId + "/retour").andExpect(status().isOk());
    }

    @Test
    void une_penalite_annulee_avec_sa_transaction_ne_bloque_personne() throws Exception {
        String adherent = inscrire();
        String exemplaire = exemplaires(1).get(0);

        new TransactionTemplate(transactions).executeWithoutResult(statut -> {
            evenements.publishEvent(new PenaliteCreee(UUID.randomUUID(), UUID.fromString(adherent), 300));
            statut.setRollbackOnly();
        });

        api.post("/emprunts", corps(adherent, exemplaire)).andExpect(status().isCreated());
    }
}
