package lydevtech.sunubiblio.adherent;

public class QuotaEmpruntsAtteintException extends RuntimeException {

    public QuotaEmpruntsAtteintException(String adherentId, int quota) {
        super("L'adhérent " + adherentId + " a déjà " + quota + " exemplaires empruntés, ce qui est son maximum : "
                + "il doit en rendre un avant d'en emprunter un autre.");
    }
}
