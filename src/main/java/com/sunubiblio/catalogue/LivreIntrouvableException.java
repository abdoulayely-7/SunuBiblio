package com.sunubiblio.catalogue;

import java.util.UUID;

public class LivreIntrouvableException extends RuntimeException {

    public LivreIntrouvableException(UUID livreId) {
        super("Livre introuvable : " + livreId);
    }
}
