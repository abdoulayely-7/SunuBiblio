package lydevtech.sunubiblio.notifications.internal.application;

import lydevtech.sunubiblio.emprunts.EmpruntEnregistre;
import lydevtech.sunubiblio.emprunts.ExemplaireRendu;
import lydevtech.sunubiblio.penalites.PenaliteCreee;
import lydevtech.sunubiblio.penalites.PenalitePayee;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Les messages ne partent qu'apres le commit de la transaction qui a publie l'evenement :
 * un emprunt ou un paiement annule ne previent personne.
 */
@Component
class EcouteursEvenements {

    private final NotificationsService notifications;

    EcouteursEvenements(NotificationsService notifications) {
        this.notifications = notifications;
    }

    @TransactionalEventListener(fallbackExecution = true)
    void surEmpruntEnregistre(EmpruntEnregistre evenement) {
        notifications.prevenir(evenement.adherentId(), "Emprunt enregistré : l'exemplaire "
                + evenement.exemplaireId() + " est à rendre avant le " + evenement.dateEcheance() + ".");
    }

    @TransactionalEventListener(fallbackExecution = true)
    void surExemplaireRendu(ExemplaireRendu evenement) {
        String message = evenement.joursDeRetard() > 0
                ? "Exemplaire " + evenement.exemplaireId() + " rendu avec " + evenement.joursDeRetard()
                + " jour(s) de retard."
                : "Exemplaire " + evenement.exemplaireId() + " rendu à temps, merci.";
        notifications.prevenir(evenement.adherentId(), message);
    }

    @TransactionalEventListener(fallbackExecution = true)
    void surPenaliteCreee(PenaliteCreee evenement) {
        notifications.prevenir(evenement.adherentId(), "Une pénalité de " + evenement.montantXof()
                + " XOF a été créée : vous ne pouvez plus emprunter tant qu'elle n'est pas payée.");
    }

    @TransactionalEventListener(fallbackExecution = true)
    void surPenalitePayee(PenalitePayee evenement) {
        notifications.prevenir(evenement.adherentId(), "Votre pénalité de " + evenement.montantXof()
                + " XOF est payée : vous pouvez de nouveau emprunter.");
    }
}
