package lydevtech.sunubiblio.emprunts;

import java.time.LocalDate;

public class DateRetourInvalideException extends RuntimeException {

    public DateRetourInvalideException(LocalDate dateRetour, LocalDate dateEmprunt) {
        super("La date de retour (" + dateRetour + ") ne peut pas précéder la date d'emprunt (" + dateEmprunt
                + ") : indiquez une date postérieure ou égale.");
    }
}
