package lydevtech.sunubiblio.catalogue;

import java.util.UUID;

public class ExemplaireIntrouvableException extends RuntimeException {

    public ExemplaireIntrouvableException(UUID exemplaireId) {
        super("Aucun exemplaire ne correspond à l'identifiant " + exemplaireId
                + " : vérifiez-le dans la fiche du livre.");
    }
}
