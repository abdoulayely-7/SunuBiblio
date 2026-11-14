package lydevtech.sunubiblio.catalogue.internal.web;

import lydevtech.sunubiblio.catalogue.CatalogueApi;
import lydevtech.sunubiblio.catalogue.LivreDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogue")
public class CatalogueController {

    private final CatalogueApi catalogueApi;

    public CatalogueController(CatalogueApi catalogueApi) {
        this.catalogueApi = catalogueApi;
    }

    public record AjouterLivreRequest(
            String isbn,
            String titre,
            String auteur,
            int nombreExemplaires
    ) {}

    @PostMapping("/livres")
    @ResponseStatus(HttpStatus.CREATED)
    public LivreDto ajouterLivre(@RequestBody AjouterLivreRequest request) {
        return catalogueApi.ajouterLivre(request.isbn(), request.titre(), request.auteur(), request.nombreExemplaires());
    }

    @GetMapping("/livres")
    public List<LivreDto> listerLivres() {
        return catalogueApi.listerLivres();
    }

    @GetMapping("/livres/{isbn}")
    public LivreDto getLivre(@PathVariable String isbn) {
        return catalogueApi.trouverLivreParIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable avec l'ISBN: " + isbn));
    }

    @DeleteMapping("/exemplaires/{exemplaireId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerExemplaire(@PathVariable String exemplaireId) {
        catalogueApi.retirerExemplaire(exemplaireId);
    }
}

