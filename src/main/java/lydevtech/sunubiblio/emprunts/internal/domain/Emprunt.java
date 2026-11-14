package lydevtech.sunubiblio.emprunts.internal.domain;

import lydevtech.sunubiblio.emprunts.DateRetourInvalideException;
import lydevtech.sunubiblio.emprunts.EmpruntDejaRenduException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record Emprunt(UUID id, UUID adherentId, UUID exemplaireId, LocalDate dateEmprunt,
                      LocalDate dateEcheance, LocalDate dateRetour) {

    /** Duree d'un pret, definie a cet unique endroit. */
    public static final int DUREE_PRET_JOURS = 14;

    public static Emprunt ouvrir(UUID adherentId, UUID exemplaireId, LocalDate dateEmprunt) {
        return new Emprunt(UUID.randomUUID(), adherentId, exemplaireId, dateEmprunt,
                dateEmprunt.plusDays(DUREE_PRET_JOURS), null);
    }

    public boolean enCours() {
        return dateRetour == null;
    }

    public Emprunt rendu(LocalDate date) {
        if (!enCours()) {
            throw new EmpruntDejaRenduException(id);
        }
        if (date.isBefore(dateEmprunt)) {
            throw new DateRetourInvalideException(date, dateEmprunt);
        }
        return new Emprunt(id, adherentId, exemplaireId, dateEmprunt, dateEcheance, date);
    }

    /** Nombre de jours apres l'echeance ; un retour le jour de l'echeance ou avant n'est pas en retard. */
    public int joursDeRetard() {
        if (enCours()) {
            return 0;
        }
        return (int) Math.max(0, ChronoUnit.DAYS.between(dateEcheance, dateRetour));
    }
}
