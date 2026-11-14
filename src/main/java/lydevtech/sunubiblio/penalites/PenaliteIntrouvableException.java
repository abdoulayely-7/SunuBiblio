package lydevtech.sunubiblio.penalites;

import java.util.UUID;

public class PenaliteIntrouvableException extends RuntimeException {

    public PenaliteIntrouvableException(UUID penaliteId) {
        super("Aucune pénalité ne correspond à l'identifiant " + penaliteId + " : vérifiez-le dans la situation de l'adhérent.");
    }
}
