package lydevtech.sunubiblio;

import lydevtech.sunubiblio.penalites.PenaliteCreee;
import lydevtech.sunubiblio.penalites.PenaliteDto;
import lydevtech.sunubiblio.penalites.PenalitePayee;
import lydevtech.sunubiblio.penalites.PenalitesApi;
import lydevtech.sunubiblio.penalites.StatutPenalite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RecordApplicationEvents
class PenalitesIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PenalitesApi penalites;

    @Autowired
    private ApplicationEvents evenements;

    private ApiClient api;

    @BeforeEach
    void preparer() {
        api = new ApiClient(mvc);
    }

    @Test
    void F7_une_penalite_de_3_jours_vaut_300_XOF_est_impayee_et_annonce_PenaliteCreee() {
        UUID adherentId = UUID.randomUUID();

        PenaliteDto penalite = penalites.creerPourRetard(adherentId, UUID.randomUUID(), 3);

        assertEquals(300, penalite.montantXof());
        assertEquals(StatutPenalite.IMPAYEE, penalite.statut());
        assertEquals(1, evenements.stream(PenaliteCreee.class)
                .filter(e -> e.penaliteId().equals(penalite.id()) && e.montantXof() == 300).count());
    }

    @Test
    void F8_payer_une_penalite_la_solde_et_annonce_PenalitePayee() throws Exception {
        UUID adherentId = UUID.randomUUID();
        PenaliteDto penalite = penalites.creerPourRetard(adherentId, UUID.randomUUID(), 2);

        api.post("/penalites/" + penalite.id() + "/paiement")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("PAYEE"))
                .andExpect(jsonPath("$.montantXof").value(200));

        assertEquals(1, evenements.stream(PenalitePayee.class)
                .filter(e -> e.penaliteId().equals(penalite.id())).count());
    }

    @Test
    void payer_deux_fois_donne_409_et_une_penalite_inconnue_donne_404() throws Exception {
        PenaliteDto penalite = penalites.creerPourRetard(UUID.randomUUID(), UUID.randomUUID(), 1);

        api.post("/penalites/" + penalite.id() + "/paiement").andExpect(status().isOk());
        api.post("/penalites/" + penalite.id() + "/paiement").andExpect(status().isConflict());
        api.post("/penalites/00000000-0000-0000-0000-000000000000/paiement").andExpect(status().isNotFound());
    }

    @Test
    void les_penalites_d_un_adherent_ne_concernent_que_lui() {
        UUID adherentA = UUID.randomUUID();
        UUID adherentB = UUID.randomUUID();
        penalites.creerPourRetard(adherentA, UUID.randomUUID(), 1);

        assertEquals(1, penalites.penalitesDe(adherentA).size());
        assertTrue(penalites.penalitesDe(adherentB).isEmpty());
    }
}
