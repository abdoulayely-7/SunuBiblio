package lydevtech.sunubiblio.pret;

public class EmpruntIntrouvableException extends RuntimeException {

    public EmpruntIntrouvableException(String empruntId) {
        super("Aucun emprunt ne correspond à l'identifiant " + empruntId + " : vérifiez-le dans la liste des emprunts en cours.");
    }
}
