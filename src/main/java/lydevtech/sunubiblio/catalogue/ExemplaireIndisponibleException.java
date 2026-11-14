package lydevtech.sunubiblio.catalogue;

import java.util.UUID;

public class ExemplaireIndisponibleException extends RuntimeException {

    public ExemplaireIndisponibleException(UUID exemplaireId) {
        super("L'exemplaire " + exemplaireId + " n'est pas disponible");
    }
}
