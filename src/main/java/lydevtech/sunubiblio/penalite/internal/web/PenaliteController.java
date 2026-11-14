package lydevtech.sunubiblio.penalite.internal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lydevtech.sunubiblio.penalite.PenaliteApi;
import lydevtech.sunubiblio.penalite.PenaliteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/penalites")
@Tag(name = "Pénalités", description = "Paiement et consultation des pénalités de retard (F8, F9)")
public class PenaliteController {

    private final PenaliteApi penaliteApi;

    public PenaliteController(PenaliteApi penaliteApi) {
        this.penaliteApi = penaliteApi;
    }

    @PostMapping("/{id}/payer")
    @Operation(summary = "Payer une pénalité (F8)",
            description = "Une fois toutes ses pénalités réglées, l'adhérent peut de nouveau emprunter.")
    @ApiResponse(responseCode = "204", description = "Pénalité réglée")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    public ResponseEntity<Void> payerPenalite(@PathVariable String id) {
        penaliteApi.reglerPenalite(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Consulter les pénalités d'un adhérent (F9)")
    @ApiResponse(responseCode = "200", description = "Toutes les pénalités de l'adhérent, des plus anciennes aux plus récentes")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    public List<PenaliteDto> getPenalitesAdherent(
            @Parameter(description = "Identifiant de l'adhérent") @RequestParam String adherentId) {
        return penaliteApi.getPenalitesAdherent(adherentId);
    }
}
