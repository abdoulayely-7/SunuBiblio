package com.sunubiblio.penalites.internal.infrastructure.web;

import com.sunubiblio.penalites.PenaliteDto;
import com.sunubiblio.penalites.PenalitesApi;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class PenalitesController {

    private final PenalitesApi penalites;

    PenalitesController(PenalitesApi penalites) {
        this.penalites = penalites;
    }

    @PostMapping("/penalites/{penaliteId}/paiement")
    PenaliteDto payer(@PathVariable UUID penaliteId) {
        return penalites.payer(penaliteId);
    }
}
