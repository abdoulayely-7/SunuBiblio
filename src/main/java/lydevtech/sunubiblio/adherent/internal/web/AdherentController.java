package lydevtech.sunubiblio.adherent.internal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lydevtech.sunubiblio.adherent.AdherentApi;
import lydevtech.sunubiblio.adherent.AdherentDto;
import lydevtech.sunubiblio.adherent.TypeAdherent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/adherents")
@Tag(name = "Adhérents", description = "Inscription des étudiants et des enseignants (F3)")
public class AdherentController {

    private final AdherentApi adherentApi;

    public AdherentController(AdherentApi adherentApi) {
        this.adherentApi = adherentApi;
    }

    public record InscriptionRequest(
            @NotBlank(message = "Le nom de l'adhérent est obligatoire.")
            @Size(max = 255, message = "Le nom de l'adhérent ne doit pas dépasser 255 caractères.")
            String nom,

            @NotNull(message = "Le type d'adhérent est obligatoire : ETUDIANT ou ENSEIGNANT.")
            TypeAdherent type) {
    }

    @PostMapping
    @Operation(summary = "S'inscrire comme adhérent, étudiant ou enseignant (F3)",
            description = "Le quota d'emprunts simultanés est de 3 pour un étudiant et de 5 pour un enseignant.")
    @ApiResponse(responseCode = "201", description = "Adhérent inscrit")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    public ResponseEntity<AdherentDto> inscrire(@Valid @RequestBody InscriptionRequest request) {
        AdherentDto adherent = adherentApi.inscrire(request.nom(), request.type());
        return ResponseEntity.created(URI.create("/api/adherents/" + adherent.id())).body(adherent);
    }

    @GetMapping("/{adherentId}")
    @Operation(summary = "Consulter un adhérent, son quota et son éventuel blocage")
    @ApiResponse(responseCode = "200", description = "L'adhérent")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    public AdherentDto trouver(@PathVariable String adherentId) {
        return adherentApi.trouver(adherentId);
    }
}
