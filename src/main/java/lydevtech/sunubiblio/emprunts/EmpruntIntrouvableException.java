package lydevtech.sunubiblio.emprunts;

import java.util.UUID;

public class EmpruntIntrouvableException extends RuntimeException {

    public EmpruntIntrouvableException(UUID empruntId) {
        super("Aucun emprunt ne correspond à l'identifiant " + empruntId + " : vérifiez-le dans la liste des emprunts en cours.");
    }
}
