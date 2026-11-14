package lydevtech.sunubiblio.catalogue;

public class ExemplaireIndisponibleException extends RuntimeException {

    public ExemplaireIndisponibleException(String exemplaireId, StatutExemplaire statut) {
        super("L'exemplaire " + exemplaireId + " ne peut pas être emprunté : "
                + (statut == StatutExemplaire.RETIRE
                ? "il a été retiré du catalogue. Choisissez un autre exemplaire du livre."
                : "il est déjà emprunté. Choisissez un autre exemplaire ou attendez son retour."));
    }
}
