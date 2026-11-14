package com.sunubiblio.adherents.internal.infrastructure.web;

import com.sunubiblio.adherents.AdherentDto;
import com.sunubiblio.adherents.AdherentsApi;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
class AdherentsController {

    private final AdherentsApi adherents;

    AdherentsController(AdherentsApi adherents) {
        this.adherents = adherents;
    }

    @PostMapping("/adherents")
    ResponseEntity<AdherentDto> inscrire(@Valid @RequestBody InscriptionRequest requete) {
        AdherentDto adherent = adherents.inscrire(requete.nom(), requete.type());
        return ResponseEntity.created(URI.create("/adherents/" + adherent.id())).body(adherent);
    }

    @GetMapping("/adherents/{adherentId}")
    AdherentDto trouver(@PathVariable UUID adherentId) {
        return adherents.trouver(adherentId);
    }
}
