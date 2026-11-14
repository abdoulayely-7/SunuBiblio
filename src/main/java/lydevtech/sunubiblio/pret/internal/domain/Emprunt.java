package lydevtech.sunubiblio.pret.internal.domain;

import lydevtech.sunubiblio.pret.DateRetourInvalideException;
import lydevtech.sunubiblio.pret.EmpruntDejaRestitueException;
import lydevtech.sunubiblio.pret.StatutEmprunt;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record Emprunt(String id, String adherentId, String exemplaireId, LocalDate dateEmprunt,
                      LocalDate dateEcheance, LocalDate dateRetour) {

    /** Durée d'un prêt, définie à cet unique endroit. */
    public static final int DUREE_PRET_JOURS = 14;

    public static Emprunt ouvrir(String id, String adherentId, String exemplaireId, LocalDate dateEmprunt) {
        return new Emprunt(id, adherentId, exemplaireId, dateEmprunt, dateEmprunt.plusDays(DUREE_PRET_JOURS), null);
    }

    public StatutEmprunt statut() {
        return dateRetour == null ? StatutEmprunt.EN_COURS : StatutEmprunt.RESTITUE;
    }

    public Emprunt restituer(LocalDate date) {
        if (statut() == StatutEmprunt.RESTITUE) {
            throw new EmpruntDejaRestitueException(id);
        }
        if (date.isBefore(dateEmprunt)) {
            throw new DateRetourInvalideException(date, dateEmprunt);
        }
        return new Emprunt(id, adherentId, exemplaireId, dateEmprunt, dateEcheance, date);
    }

    /** Nombre de jours après l'échéance ; un retour le jour de l'échéance ou avant n'est pas en retard. */
    public int joursDeRetard() {
        if (dateRetour == null) {
            return 0;
        }
        return (int) Math.max(0, ChronoUnit.DAYS.between(dateEcheance, dateRetour));
    }
}
