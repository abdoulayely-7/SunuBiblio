package lydevtech.sunubiblio.emprunts.internal.application;

import lydevtech.sunubiblio.emprunts.internal.domain.BlocageRepository;
import lydevtech.sunubiblio.penalites.PenaliteCreee;
import lydevtech.sunubiblio.penalites.PenalitePayee;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Le pret est l'unique point de passage : il ecoute les evenements de penalites pour savoir
 * qui est bloque. L'ecoute est synchrone et dans la transaction de l'emetteur : le blocage
 * est enregistre ou annule en meme temps que la penalite.
 */
@Component
class EcouteurPenalites {

    private final BlocageRepository blocages;

    EcouteurPenalites(BlocageRepository blocages) {
        this.blocages = blocages;
    }

    @EventListener
    void surPenaliteCreee(PenaliteCreee evenement) {
        blocages.bloquer(evenement.penaliteId(), evenement.adherentId());
    }

    @EventListener
    void surPenalitePayee(PenalitePayee evenement) {
        blocages.debloquer(evenement.penaliteId());
    }
}
