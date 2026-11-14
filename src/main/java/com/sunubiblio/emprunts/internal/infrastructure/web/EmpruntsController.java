package com.sunubiblio.emprunts.internal.infrastructure.web;

import com.sunubiblio.emprunts.EmpruntDto;
import com.sunubiblio.emprunts.EmpruntsApi;
import com.sunubiblio.emprunts.RetourDto;
import com.sunubiblio.emprunts.SituationAdherentDto;
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
class EmpruntsController {

    private final EmpruntsApi emprunts;

    EmpruntsController(EmpruntsApi emprunts) {
        this.emprunts = emprunts;
    }

    @PostMapping("/emprunts")
    ResponseEntity<EmpruntDto> emprunter(@Valid @RequestBody EmpruntRequest requete) {
        EmpruntDto emprunt = emprunts.emprunter(requete.adherentId(), requete.exemplaireId());
        return ResponseEntity.created(URI.create("/emprunts/" + emprunt.id())).body(emprunt);
    }

    @PostMapping("/emprunts/{empruntId}/retour")
    RetourDto rendre(@PathVariable UUID empruntId, @RequestBody(required = false) RetourRequest requete) {
        return emprunts.rendre(empruntId, requete == null ? null : requete.dateRetour());
    }

    @GetMapping("/emprunts/{empruntId}")
    EmpruntDto trouver(@PathVariable UUID empruntId) {
        return emprunts.trouver(empruntId);
    }

    @GetMapping("/situations/{adherentId}")
    SituationAdherentDto situation(@PathVariable UUID adherentId) {
        return emprunts.situation(adherentId);
    }
}
