package lydevtech.sunubiblio.emprunts;

import java.util.UUID;

public class QuotaEmpruntsAtteintException extends RuntimeException {

    public QuotaEmpruntsAtteintException(UUID adherentId, int quota) {
        super("L'adhérent " + adherentId + " a déjà " + quota + " exemplaires empruntés, ce qui est son maximum : "
                + "il doit en rendre un avant d'en emprunter un autre.");
    }
}
