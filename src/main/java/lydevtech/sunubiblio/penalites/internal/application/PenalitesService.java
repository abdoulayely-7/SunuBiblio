package lydevtech.sunubiblio.penalites.internal.application;

import lydevtech.sunubiblio.penalites.PenaliteCreee;
import lydevtech.sunubiblio.penalites.PenaliteDejaPayeeException;
import lydevtech.sunubiblio.penalites.PenaliteDto;
import lydevtech.sunubiblio.penalites.PenaliteIntrouvableException;
import lydevtech.sunubiblio.penalites.PenalitePayee;
import lydevtech.sunubiblio.penalites.PenalitesApi;
import lydevtech.sunubiblio.penalites.StatutPenalite;
import lydevtech.sunubiblio.penalites.internal.domain.Penalite;
import lydevtech.sunubiblio.penalites.internal.domain.PenaliteRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
class PenalitesService implements PenalitesApi {

    private final PenaliteRepository penalites;
    private final ApplicationEventPublisher evenements;

    PenalitesService(PenaliteRepository penalites, ApplicationEventPublisher evenements) {
        this.penalites = penalites;
        this.evenements = evenements;
    }

    @Override
    public PenaliteDto creerPourRetard(UUID adherentId, UUID empruntId, int joursDeRetard) {
        Penalite penalite = Penalite.pourRetard(adherentId, empruntId, joursDeRetard);
        penalites.ajouter(penalite);
        evenements.publishEvent(new PenaliteCreee(penalite.id(), adherentId, penalite.montantXof()));
        return versDto(penalite);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean aPenaliteImpayee(UUID adherentId) {
        return penalites.existeImpayee(adherentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaliteDto> penalitesDe(UUID adherentId) {
        return penalites.parAdherent(adherentId).stream().map(PenalitesService::versDto).toList();
    }

    @Override
    public PenaliteDto payer(UUID penaliteId) {
        Penalite penalite = penalites.trouver(penaliteId).orElseThrow(() -> new PenaliteIntrouvableException(penaliteId));
        Penalite payee = penalite.payee();
        if (!penalites.changerStatut(penaliteId, StatutPenalite.IMPAYEE, StatutPenalite.PAYEE)) {
            throw new PenaliteDejaPayeeException(penaliteId);
        }
        evenements.publishEvent(new PenalitePayee(penaliteId, penalite.adherentId(), penalite.montantXof()));
        return versDto(payee);
    }

    private static PenaliteDto versDto(Penalite p) {
        return new PenaliteDto(p.id(), p.adherentId(), p.empruntId(), p.joursDeRetard(), p.montantXof(), p.statut());
    }
}
