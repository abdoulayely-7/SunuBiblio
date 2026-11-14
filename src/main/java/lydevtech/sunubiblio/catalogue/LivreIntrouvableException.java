package lydevtech.sunubiblio.catalogue;

public class LivreIntrouvableException extends RuntimeException {

    public LivreIntrouvableException(String isbn) {
        super("Aucun livre ne correspond à l'ISBN " + isbn + " : vérifiez-le ou recherchez le livre dans le catalogue.");
    }
}
