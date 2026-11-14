package lydevtech.sunubiblio.adherents;

import java.util.UUID;

public class AdherentIntrouvableException extends RuntimeException {

    public AdherentIntrouvableException(UUID adherentId) {
        super("Aucun adhérent ne correspond à l'identifiant " + adherentId
                + " : vérifiez-le ou inscrivez l'adhérent avant de continuer.");
    }
}
