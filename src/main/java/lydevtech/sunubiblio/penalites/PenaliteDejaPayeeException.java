package lydevtech.sunubiblio.penalites;

import java.util.UUID;

public class PenaliteDejaPayeeException extends RuntimeException {

    public PenaliteDejaPayeeException(UUID penaliteId) {
        super("La pénalité " + penaliteId + " est déjà payée : elle ne peut pas être payée deux fois.");
    }
}
