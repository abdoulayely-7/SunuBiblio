package lydevtech.sunubiblio.penalite.internal.application;

import lydevtech.sunubiblio.penalite.PenaliteApi;
import lydevtech.sunubiblio.penalite.PenaliteDto;
import lydevtech.sunubiblio.penalite.PenaliteIntrouvableException;
import lydevtech.sunubiblio.penalite.PenaliteStatutChangeEvent;
import lydevtech.sunubiblio.penalite.internal.domain.Penalite;
import lydevtech.sunubiblio.penalite.internal.domain.PenaliteRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PenaliteService implements PenaliteApi {

    private final PenaliteRepository penaliteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PenaliteService(PenaliteRepository penaliteRepository, ApplicationEventPublisher eventPublisher) {
        this.penaliteRepository = penaliteRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PenaliteDto creerPenalite(String adherentId, String exemplaireId, long joursDeRetard) {
        String id = "PEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Penalite penalite = new Penalite(id, adherentId, exemplaireId, joursDeRetard);
        penaliteRepository.save(penalite);

        // Publier l'événement : l'adhérent a maintenant une pénalité impayée
        publierChangementStatut(adherentId);
        return toDto(penalite);
    }

    @Override
    public void reglerPenalite(String penaliteId) {
        Penalite penalite = penaliteRepository.findByIdPourModification(penaliteId)
                .orElseThrow(() -> new PenaliteIntrouvableException(penaliteId));
        penalite.regler();
        penaliteRepository.save(penalite);

        // Publier l'événement : recalculer si l'adhérent a encore d'autres impayés ou s'il est libéré
        publierChangementStatut(penalite.getAdherentId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean aDesImpayes(String adherentId) {
        return penaliteRepository.findByAdherentId(adherentId).stream()
                .anyMatch(Penalite::isImpayee);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PenaliteDto> getPenalitesAdherent(String adherentId) {
        return penaliteRepository.findByAdherentId(adherentId).stream()
                .map(this::toDto)
                .toList();
    }

    private void publierChangementStatut(String adherentId) {
        List<Penalite> penalites = penaliteRepository.findByAdherentId(adherentId);
        boolean aDesImpayes = penalites.stream().anyMatch(Penalite::isImpayee);
        long montantTotalImpaye = penalites.stream()
                .filter(Penalite::isImpayee)
                .mapToLong(Penalite::getMontant)
                .sum();
        eventPublisher.publishEvent(new PenaliteStatutChangeEvent(adherentId, aDesImpayes, montantTotalImpaye));
    }

    private PenaliteDto toDto(Penalite penalite) {
        return new PenaliteDto(
                penalite.getId(),
                penalite.getAdherentId(),
                penalite.getExemplaireId(),
                penalite.getJoursDeRetard(),
                penalite.getMontant(),
                penalite.getStatut(),
                penalite.getDateCreation()
        );
    }
}
