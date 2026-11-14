package lydevtech.sunubiblio.catalogue.internal.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lydevtech.sunubiblio.catalogue.CatalogueApi;
import lydevtech.sunubiblio.catalogue.ExemplaireDto;
import lydevtech.sunubiblio.catalogue.LivreDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Catalogue", description = "Livres et exemplaires (F1, F2, F4)")
class CatalogueController {

    private final CatalogueApi catalogue;

    CatalogueController(CatalogueApi catalogue) {
        this.catalogue = catalogue;
    }

    @PostMapping("/livres")
    @Operation(summary = "Ajouter un livre et ses exemplaires (F1)")
    @ApiResponse(responseCode = "201", description = "Livre créé, tous ses exemplaires sont disponibles")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    ResponseEntity<LivreDto> ajouterLivre(@Valid @RequestBody AjouterLivreRequest requete) {
        LivreDto livre = catalogue.ajouterLivre(requete.titre(), requete.auteur(), requete.nombreExemplaires());
        return ResponseEntity.created(URI.create("/livres/" + livre.id())).body(livre);
    }

    @PostMapping("/livres/{livreId}/exemplaires")
    @Operation(summary = "Ajouter un exemplaire à un livre existant (F1)")
    @ApiResponse(responseCode = "201", description = "Exemplaire créé, disponible")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    ResponseEntity<ExemplaireDto> ajouterExemplaire(@PathVariable UUID livreId) {
        ExemplaireDto exemplaire = catalogue.ajouterExemplaire(livreId);
        return ResponseEntity.created(URI.create("/livres/" + livreId)).body(exemplaire);
    }

    @DeleteMapping("/exemplaires/{exemplaireId}")
    @Operation(summary = "Retirer un exemplaire abîmé (F2)",
            description = "Un exemplaire en cours d'emprunt ne peut pas être retiré : il doit d'abord être rendu.")
    @ApiResponse(responseCode = "204", description = "Exemplaire retiré du catalogue")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    ResponseEntity<Void> retirerExemplaire(@PathVariable UUID exemplaireId) {
        catalogue.retirerExemplaire(exemplaireId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/livres/{livreId}")
    @Operation(summary = "Consulter un livre et l'état de ses exemplaires")
    @ApiResponse(responseCode = "200", description = "Le livre, avec le nombre d'exemplaires disponibles")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    LivreDto trouverLivre(@PathVariable UUID livreId) {
        return catalogue.trouverLivre(livreId);
    }

    @GetMapping("/livres")
    @Operation(summary = "Rechercher un livre et voir les exemplaires disponibles (F4)")
    @ApiResponse(responseCode = "200", description = "Les livres dont le titre ou l'auteur contient le texte (tous sans texte)")
    List<LivreDto> rechercher(
            @Parameter(description = "Texte cherché dans le titre ou l'auteur, sans tenir compte des majuscules")
            @RequestParam(name = "q", required = false) String texte) {
        return catalogue.rechercher(texte);
    }
}
