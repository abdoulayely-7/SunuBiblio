package lydevtech.sunubiblio.catalogue;

import java.util.UUID;

public class LivreIntrouvableException extends RuntimeException {

    public LivreIntrouvableException(UUID livreId) {
        super("Aucun livre ne correspond à l'identifiant " + livreId + " : vérifiez-le ou recherchez le livre dans le catalogue.");
    }
}
