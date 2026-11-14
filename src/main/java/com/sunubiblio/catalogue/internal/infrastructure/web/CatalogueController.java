package com.sunubiblio.catalogue.internal.infrastructure.web;

import com.sunubiblio.catalogue.CatalogueApi;
import com.sunubiblio.catalogue.ExemplaireDto;
import com.sunubiblio.catalogue.LivreDto;
import jakarta.validation.Valid;
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
class CatalogueController {

    private final CatalogueApi catalogue;

    CatalogueController(CatalogueApi catalogue) {
        this.catalogue = catalogue;
    }

    @PostMapping("/livres")
    ResponseEntity<LivreDto> ajouterLivre(@Valid @RequestBody AjouterLivreRequest requete) {
        LivreDto livre = catalogue.ajouterLivre(requete.titre(), requete.auteur(), requete.nombreExemplaires());
        return ResponseEntity.created(URI.create("/livres/" + livre.id())).body(livre);
    }

    @PostMapping("/livres/{livreId}/exemplaires")
    ResponseEntity<ExemplaireDto> ajouterExemplaire(@PathVariable UUID livreId) {
        ExemplaireDto exemplaire = catalogue.ajouterExemplaire(livreId);
        return ResponseEntity.created(URI.create("/livres/" + livreId)).body(exemplaire);
    }

    @DeleteMapping("/exemplaires/{exemplaireId}")
    ResponseEntity<Void> retirerExemplaire(@PathVariable UUID exemplaireId) {
        catalogue.retirerExemplaire(exemplaireId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/livres/{livreId}")
    LivreDto trouverLivre(@PathVariable UUID livreId) {
        return catalogue.trouverLivre(livreId);
    }

    @GetMapping("/livres")
    List<LivreDto> rechercher(@RequestParam(name = "q", required = false) String texte) {
        return catalogue.rechercher(texte);
    }
}
