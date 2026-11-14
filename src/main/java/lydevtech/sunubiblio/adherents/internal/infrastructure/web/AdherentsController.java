package lydevtech.sunubiblio.adherents.internal.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lydevtech.sunubiblio.adherents.AdherentDto;
import lydevtech.sunubiblio.adherents.AdherentsApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@Tag(name = "Adhérents", description = "Inscription des étudiants et des enseignants (F3)")
class AdherentsController {

    private final AdherentsApi adherents;

    AdherentsController(AdherentsApi adherents) {
        this.adherents = adherents;
    }

    @PostMapping("/adherents")
    @Operation(summary = "S'inscrire comme adhérent, étudiant ou enseignant (F3)",
            description = "Le quota d'emprunts simultanés est de 3 pour un étudiant et de 5 pour un enseignant.")
    @ApiResponse(responseCode = "201", description = "Adhérent inscrit")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    ResponseEntity<AdherentDto> inscrire(@Valid @RequestBody InscriptionRequest requete) {
        AdherentDto adherent = adherents.inscrire(requete.nom(), requete.type());
        return ResponseEntity.created(URI.create("/adherents/" + adherent.id())).body(adherent);
    }

    @GetMapping("/adherents/{adherentId}")
    @Operation(summary = "Consulter un adhérent")
    @ApiResponse(responseCode = "200", description = "L'adhérent et son quota d'emprunts")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    AdherentDto trouver(@PathVariable UUID adherentId) {
        return adherents.trouver(adherentId);
    }
}
