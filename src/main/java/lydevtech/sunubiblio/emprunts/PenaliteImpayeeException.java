package lydevtech.sunubiblio.emprunts;

import java.util.UUID;

public class PenaliteImpayeeException extends RuntimeException {

    public PenaliteImpayeeException(UUID adherentId) {
        super("L'adhérent " + adherentId + " a une pénalité de retard impayée : il doit d'abord la payer "
                + "avant de pouvoir emprunter de nouveau.");
    }
}
