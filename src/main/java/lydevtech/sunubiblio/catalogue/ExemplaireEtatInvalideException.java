package lydevtech.sunubiblio.catalogue;

public class ExemplaireEtatInvalideException extends RuntimeException {

    public ExemplaireEtatInvalideException(String exemplaireId, String raison) {
        super("Opération impossible sur l'exemplaire " + exemplaireId + " : " + raison);
    }
}
