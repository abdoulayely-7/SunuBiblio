package lydevtech.sunubiblio.catalogue.internal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lydevtech.sunubiblio.catalogue.CatalogueApi;
import lydevtech.sunubiblio.catalogue.LivreDto;
import lydevtech.sunubiblio.catalogue.LivreIntrouvableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/catalogue")
@Tag(name = "Catalogue", description = "Livres et exemplaires (F1, F2, F4)")
public class CatalogueController {

    private final CatalogueApi catalogueApi;

    public CatalogueController(CatalogueApi catalogueApi) {
        this.catalogueApi = catalogueApi;
    }

    public record AjouterLivreRequest(
            @NotBlank(message = "L'ISBN du livre est obligatoire.")
            @Size(max = 32, message = "L'ISBN ne doit pas dépasser 32 caractères.")
            @Pattern(regexp = "[0-9A-Za-z-]*", message = "L'ISBN ne peut contenir que des chiffres, des lettres et des tirets.")
            String isbn,

            @NotBlank(message = "Le titre du livre est obligatoire.")
            @Size(max = 255, message = "Le titre du livre ne doit pas dépasser 255 caractères.")
            String titre,

            @NotBlank(message = "L'auteur du livre est obligatoire.")
            @Size(max = 255, message = "Le nom de l'auteur ne doit pas dépasser 255 caractères.")
            String auteur,

            @NotNull(message = "Le nombre d'exemplaires est obligatoire.")
            @Min(value = 1, message = "Un livre doit avoir au moins 1 exemplaire.")
            @Max(value = 500, message = "Un livre ne peut pas être créé avec plus de 500 exemplaires à la fois.")
            Integer nombreExemplaires) {
    }

    @PostMapping("/livres")
    @Operation(summary = "Ajouter un livre et ses exemplaires (F1)",
            description = "Si l'ISBN est déjà au catalogue, les nouveaux exemplaires sont ajoutés à ce livre (son titre et son auteur ne changent pas).")
    @ApiResponse(responseCode = "201", description = "Exemplaires créés, tous disponibles")
    @ApiResponse(responseCode = "400", ref = "RequeteInvalide")
    public ResponseEntity<LivreDto> ajouterLivre(@Valid @RequestBody AjouterLivreRequest request) {
        LivreDto livre = catalogueApi.ajouterLivre(request.isbn(), request.titre(), request.auteur(), request.nombreExemplaires());
        return ResponseEntity.created(URI.create("/api/catalogue/livres/" + livre.isbn())).body(livre);
    }

    @GetMapping("/livres")
    @Operation(summary = "Lister ou rechercher les livres et voir les exemplaires disponibles (F4)")
    @ApiResponse(responseCode = "200", description = "Les livres dont le titre, l'auteur ou l'ISBN contient le texte (tous sans texte)")
    public List<LivreDto> listerLivres(
            @Parameter(description = "Texte cherché dans le titre, l'auteur ou l'ISBN, sans tenir compte des majuscules")
            @RequestParam(name = "q", required = false) String texte) {
        return catalogueApi.rechercherLivres(texte);
    }

    @GetMapping("/livres/{isbn}")
    @Operation(summary = "Consulter un livre et l'état de ses exemplaires")
    @ApiResponse(responseCode = "200", description = "Le livre et ses exemplaires")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    public LivreDto getLivre(@PathVariable String isbn) {
        return catalogueApi.trouverLivreParIsbn(isbn).orElseThrow(() -> new LivreIntrouvableException(isbn));
    }

    @DeleteMapping("/exemplaires/{exemplaireId}")
    @Operation(summary = "Retirer un exemplaire abîmé (F2)",
            description = "Un exemplaire en cours d'emprunt ne peut pas être retiré : il doit d'abord être rendu.")
    @ApiResponse(responseCode = "204", description = "Exemplaire retiré du catalogue")
    @ApiResponse(responseCode = "404", ref = "Introuvable")
    @ApiResponse(responseCode = "409", ref = "Conflit")
    public ResponseEntity<Void> retirerExemplaire(@PathVariable String exemplaireId) {
        catalogueApi.retirerExemplaire(exemplaireId);
        return ResponseEntity.noContent().build();
    }
}
