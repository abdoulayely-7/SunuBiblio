package com.sunubiblio.emprunts;

import java.util.UUID;

public class EmpruntIntrouvableException extends RuntimeException {

    public EmpruntIntrouvableException(UUID empruntId) {
        super("Emprunt introuvable : " + empruntId);
    }
}
