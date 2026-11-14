package lydevtech.sunubiblio.penalites.internal.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lydevtech.sunubiblio.penalites.PenaliteDto;
import lydevtech.sunubiblio.penalites.PenalitesApi;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Pénalités", description = "Paiement des pénalités de retard (F8)")
class PenalitesController {

    private final PenalitesApi penalites;

    PenalitesController(PenalitesApi penalites) {
        this.penalites = penalites;
    }

    @PostMapping("/penalites/{penaliteId}/paiement")
    @Operation(summary = "Payer une pénalité (F8)",
            description = "Une fois toutes ses pénalités payées, l'adhérent peut de nouveau emprunter.")
    @ApiResponse(responseCode = "200", description = "Pénalité payée")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    PenaliteDto payer(@PathVariable UUID penaliteId) {
        return penalites.payer(penaliteId);
    }
}
