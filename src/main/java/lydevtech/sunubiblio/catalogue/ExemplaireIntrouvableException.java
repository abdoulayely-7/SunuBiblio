package lydevtech.sunubiblio.catalogue;

import java.util.UUID;

public class ExemplaireIntrouvableException extends RuntimeException {

    public ExemplaireIntrouvableException(UUID exemplaireId) {
        super("Exemplaire introuvable : " + exemplaireId);
    }
}
