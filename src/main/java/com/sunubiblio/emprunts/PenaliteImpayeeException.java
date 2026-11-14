package com.sunubiblio.emprunts;

import java.util.UUID;

public class PenaliteImpayeeException extends RuntimeException {

    public PenaliteImpayeeException(UUID adherentId) {
        super("L'adhérent " + adherentId + " a une pénalité impayée : il ne peut plus emprunter");
    }
}
