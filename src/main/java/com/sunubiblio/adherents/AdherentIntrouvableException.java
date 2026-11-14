package com.sunubiblio.adherents;

import java.util.UUID;

public class AdherentIntrouvableException extends RuntimeException {

    public AdherentIntrouvableException(UUID adherentId) {
        super("Adhérent introuvable : " + adherentId);
    }
}
