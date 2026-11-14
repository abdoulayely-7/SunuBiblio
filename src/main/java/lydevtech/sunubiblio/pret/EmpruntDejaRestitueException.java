package lydevtech.sunubiblio.pret;

public class EmpruntDejaRestitueException extends RuntimeException {

    public EmpruntDejaRestitueException(String empruntId) {
        super("L'emprunt " + empruntId + " a déjà été restitué : un exemplaire ne peut pas être rendu deux fois.");
    }
}
