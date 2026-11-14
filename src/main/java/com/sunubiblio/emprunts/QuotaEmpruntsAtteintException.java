package com.sunubiblio.emprunts;

import java.util.UUID;

public class QuotaEmpruntsAtteintException extends RuntimeException {

    public QuotaEmpruntsAtteintException(UUID adherentId, int quota) {
        super("L'adhérent " + adherentId + " a déjà atteint son quota de " + quota + " exemplaires empruntés");
    }
}
