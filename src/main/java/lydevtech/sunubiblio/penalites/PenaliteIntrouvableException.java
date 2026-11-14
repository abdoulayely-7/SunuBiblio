package lydevtech.sunubiblio.penalites;

import java.util.UUID;

public class PenaliteIntrouvableException extends RuntimeException {

    public PenaliteIntrouvableException(UUID penaliteId) {
        super("Pénalité introuvable : " + penaliteId);
    }
}
