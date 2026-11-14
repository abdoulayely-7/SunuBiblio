package lydevtech.sunubiblio.pret.internal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lydevtech.sunubiblio.pret.EmpruntDto;
import lydevtech.sunubiblio.pret.PretApi;
import lydevtech.sunubiblio.pret.RestitutionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/prets")
@Tag(name = "Prêts", description = "Emprunts, restitutions et emprunts en cours (F5, F6, F7, F9)")
public class PretController {

    private final PretApi pretApi;

    public PretController(PretApi pretApi) {
        this.pretApi = pretApi;
    }

    public record EmpruntRequest(
            @NotBlank(message = "L'identifiant de l'adhérent (adherentId) est obligatoire.")
            String adherentId,

            @NotBlank(message = "L'identifiant de l'exemplaire (exemplaireId) est obligatoire.")
            String exemplaireId) {
    }

    /** Corps optionnel : sans date, c'est la date du jour (la date permet de rejouer un retard à la main). */
    public record RestitutionRequest(LocalDate dateRetour) {
    }

    @PostMapping
    @Operation(summary = "Emprunter un exemplaire (F5)",
            description = "Le prêt dure 14 jours. Refusé si l'adhérent a atteint son quota, s'il a une pénalité "
                    + "impayée ou si l'exemplaire n'est pas disponible.")
    @ApiResponse(responseCode = "201", description = "Emprunt enregistré, avec sa date d'échéance")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    public ResponseEntity<EmpruntDto> emprunter(@Valid @RequestBody EmpruntRequest request) {
        EmpruntDto emprunt = pretApi.emprunter(request.adherentId(), request.exemplaireId());
        return ResponseEntity.created(URI.create("/api/prets/" + emprunt.id())).body(emprunt);
    }

    @PostMapping("/{empruntId}/restitution")
    @Operation(summary = "Rendre un exemplaire (F6, F7)",
            description = "Sans corps, la date de retour est celle du jour. Un retour après l'échéance crée une "
                    + "pénalité de 100 XOF par jour de retard.")
    @ApiResponse(responseCode = "200", description = "Restitution enregistrée ; la pénalité est absente si l'exemplaire est rendu à temps")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    public RestitutionDto restituer(@PathVariable String empruntId,
                                    @RequestBody(required = false) RestitutionRequest request) {
        return pretApi.restituer(empruntId, request == null ? null : request.dateRetour());
    }

    @GetMapping
    @Operation(summary = "Consulter les emprunts en cours d'un adhérent (F9)")
    @ApiResponse(responseCode = "200", description = "Les emprunts non rendus de l'adhérent, du plus ancien au plus récent")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    public List<EmpruntDto> getEmpruntsEnCours(
            @Parameter(description = "Identifiant de l'adhérent") @RequestParam String adherentId) {
        return pretApi.getEmpruntsEnCours(adherentId);
    }

    @GetMapping("/{empruntId}")
    @Operation(summary = "Consulter un emprunt")
    @ApiResponse(responseCode = "200", description = "L'emprunt")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    public EmpruntDto trouver(@PathVariable String empruntId) {
        return pretApi.trouver(empruntId);
    }
}
