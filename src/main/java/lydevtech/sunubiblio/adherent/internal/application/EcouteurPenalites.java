package lydevtech.sunubiblio.adherent.internal.application;

import lydevtech.sunubiblio.penalite.PenaliteStatutChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Le module penalite annonce l'état des pénalités d'un adhérent ; l'adhérent en déduit s'il est bloqué.
 * L'écoute est synchrone et dans la transaction de l'émetteur : le blocage est enregistré, ou annulé,
 * en même temps que la pénalité.
 */
@Component
class EcouteurPenalites {

    private final AdherentService adherents;

    EcouteurPenalites(AdherentService adherents) {
        this.adherents = adherents;
    }

    @EventListener
    void surChangementDeStatut(PenaliteStatutChangeEvent evenement) {
        adherents.mettreAJourImpayes(evenement.adherentId(), evenement.aDesImpayes(), evenement.montantTotalImpaye());
    }
}
