package com.sunubiblio.catalogue;

import java.util.UUID;

public class ExemplaireEtatInvalideException extends RuntimeException {

    public ExemplaireEtatInvalideException(UUID exemplaireId, String raison) {
        super("Opération impossible sur l'exemplaire " + exemplaireId + " : " + raison);
    }
}
