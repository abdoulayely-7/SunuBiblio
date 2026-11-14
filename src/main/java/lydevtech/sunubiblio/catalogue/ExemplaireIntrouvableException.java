package lydevtech.sunubiblio.catalogue;

public class ExemplaireIntrouvableException extends RuntimeException {

    public ExemplaireIntrouvableException(String exemplaireId) {
        super("Aucun exemplaire ne correspond à l'identifiant " + exemplaireId + " : vérifiez-le dans la fiche du livre.");
    }
}
