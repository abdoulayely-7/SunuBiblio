package lydevtech.sunubiblio.adherent;

public class AdherentIntrouvableException extends RuntimeException {

    public AdherentIntrouvableException(String adherentId) {
        super("Aucun adhérent ne correspond à l'identifiant " + adherentId
                + " : vérifiez-le ou inscrivez l'adhérent avant de continuer.");
    }
}
