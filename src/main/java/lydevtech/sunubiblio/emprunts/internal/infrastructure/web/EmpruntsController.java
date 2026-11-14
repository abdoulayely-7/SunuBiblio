package lydevtech.sunubiblio.emprunts.internal.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lydevtech.sunubiblio.emprunts.EmpruntDto;
import lydevtech.sunubiblio.emprunts.EmpruntsApi;
import lydevtech.sunubiblio.emprunts.RetourDto;
import lydevtech.sunubiblio.emprunts.SituationAdherentDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@Tag(name = "Emprunts", description = "Prêts, retours et situation d'un adhérent (F5, F6, F7, F9)")
class EmpruntsController {

    private final EmpruntsApi emprunts;

    EmpruntsController(EmpruntsApi emprunts) {
        this.emprunts = emprunts;
    }

    @PostMapping("/emprunts")
    @Operation(summary = "Emprunter un exemplaire (F5)",
            description = "Le prêt dure 14 jours. Refusé si l'adhérent a atteint son quota, s'il a une pénalité "
                    + "impayée ou si l'exemplaire n'est pas disponible.")
    @ApiResponse(responseCode = "201", description = "Emprunt enregistré, avec sa date d'échéance")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    ResponseEntity<EmpruntDto> emprunter(@Valid @RequestBody EmpruntRequest requete) {
        EmpruntDto emprunt = emprunts.emprunter(requete.adherentId(), requete.exemplaireId());
        return ResponseEntity.created(URI.create("/emprunts/" + emprunt.id())).body(emprunt);
    }

    @PostMapping("/emprunts/{empruntId}/retour")
    @Operation(summary = "Rendre un exemplaire (F6, F7)",
            description = "Sans corps, la date de retour est celle du jour. Un retour après l'échéance crée une "
                    + "pénalité de 100 XOF par jour de retard.")
    @ApiResponse(responseCode = "200", description = "Retour enregistré ; la pénalité est absente si l'exemplaire est rendu à temps")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    RetourDto rendre(@PathVariable UUID empruntId, @RequestBody(required = false) RetourRequest requete) {
        return emprunts.rendre(empruntId, requete == null ? null : requete.dateRetour());
    }

    @GetMapping("/emprunts/{empruntId}")
    @Operation(summary = "Consulter un emprunt")
    @ApiResponse(responseCode = "200", description = "L'emprunt")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    EmpruntDto trouver(@PathVariable UUID empruntId) {
        return emprunts.trouver(empruntId);
    }

    @GetMapping("/situations/{adherentId}")
    @Operation(summary = "Consulter ses emprunts en cours et ses pénalités (F9)")
    @ApiResponse(responseCode = "200", description = "Les emprunts en cours et toutes les pénalités de l'adhérent")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    SituationAdherentDto situation(@PathVariable UUID adherentId) {
        return emprunts.situation(adherentId);
    }
}
