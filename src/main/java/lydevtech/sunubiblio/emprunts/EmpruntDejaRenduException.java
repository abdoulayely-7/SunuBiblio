package lydevtech.sunubiblio.emprunts;

import java.util.UUID;

public class EmpruntDejaRenduException extends RuntimeException {

    public EmpruntDejaRenduException(UUID empruntId) {
        super("L'emprunt " + empruntId + " a déjà été rendu : un exemplaire ne peut pas être rendu deux fois.");
    }
}
