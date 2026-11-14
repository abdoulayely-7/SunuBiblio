package lydevtech.sunubiblio.catalogue;

import java.util.UUID;

public class ExemplaireIndisponibleException extends RuntimeException {

    public ExemplaireIndisponibleException(UUID exemplaireId, EtatExemplaire etat) {
        super("L'exemplaire " + exemplaireId + " ne peut pas être emprunté : "
                + (etat == EtatExemplaire.RETIRE
                ? "il a été retiré du catalogue. Choisissez un autre exemplaire du livre."
                : "il est déjà emprunté. Choisissez un autre exemplaire ou attendez son retour."));
    }

    public ExemplaireIndisponibleException(UUID exemplaireId) {
        super("L'exemplaire " + exemplaireId + " vient d'être emprunté par un autre adhérent : "
                + "choisissez un autre exemplaire ou réessayez après son retour.");
    }
}
