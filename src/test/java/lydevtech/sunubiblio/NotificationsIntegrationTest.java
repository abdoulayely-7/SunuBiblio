package lydevtech.sunubiblio;

import lydevtech.sunubiblio.emprunts.EmpruntEnregistre;
import lydevtech.sunubiblio.emprunts.ExemplaireRendu;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationsIntegrationTest {

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

    @Test
    void chaque_evenement_annonce_previent_l_adherent_concerne() throws Exception {
        UUID adherentId = UUID.randomUUID();
        UUID exemplaireId = UUID.randomUUID();

        evenements.publishEvent(new EmpruntEnregistre(UUID.randomUUID(), adherentId, exemplaireId,
                LocalDate.of(2026, 3, 16)));
        evenements.publishEvent(new ExemplaireRendu(UUID.randomUUID(), adherentId, exemplaireId, 3));
        evenements.publishEvent(new PenaliteCreee(UUID.randomUUID(), adherentId, 300));
        evenements.publishEvent(new PenalitePayee(UUID.randomUUID(), adherentId, 300));

        api.get("/notifications?adherentId=" + adherentId)
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].message").value(containsString("à rendre avant le 2026-03-16")))
                .andExpect(jsonPath("$[1].message").value(containsString("3 jour(s) de retard")))
                .andExpect(jsonPath("$[2].message").value(containsString("pénalité de 300 XOF a été créée")))
                .andExpect(jsonPath("$[3].message").value(containsString("pénalité de 300 XOF est payée")));
    }

    @Test
    void un_retour_a_temps_est_aussi_annonce_sans_parler_de_retard() throws Exception {
        UUID adherentId = UUID.randomUUID();

        evenements.publishEvent(new ExemplaireRendu(UUID.randomUUID(), adherentId, UUID.randomUUID(), 0));

        api.get("/notifications?adherentId=" + adherentId)
                .andExpect(jsonPath("$[0].message").value(containsString("rendu à temps")));
    }

    @Test
    void une_transaction_annulee_ne_previent_personne() throws Exception {
        UUID adherentId = UUID.randomUUID();

        new TransactionTemplate(transactions).executeWithoutResult(statut -> {
            evenements.publishEvent(new PenaliteCreee(UUID.randomUUID(), adherentId, 300));
            statut.setRollbackOnly();
        });

        api.get("/notifications?adherentId=" + adherentId).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void les_messages_d_un_adherent_ne_sont_pas_visibles_pour_un_autre() throws Exception {
        UUID adherentA = UUID.randomUUID();
        evenements.publishEvent(new PenaliteCreee(UUID.randomUUID(), adherentA, 100));

        api.get("/notifications?adherentId=" + UUID.randomUUID()).andExpect(jsonPath("$.length()").value(0));
        api.get("/notifications?adherentId=" + adherentA).andExpect(jsonPath("$.length()").value(1));
    }
}
