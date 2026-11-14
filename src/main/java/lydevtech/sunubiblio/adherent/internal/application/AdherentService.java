package lydevtech.sunubiblio.adherent.internal.application;

import lydevtech.sunubiblio.adherent.AdherentApi;
import lydevtech.sunubiblio.adherent.AdherentDto;
import lydevtech.sunubiblio.adherent.AdherentIntrouvableException;
import lydevtech.sunubiblio.adherent.TypeAdherent;
import lydevtech.sunubiblio.adherent.internal.domain.Adherent;
import lydevtech.sunubiblio.adherent.internal.domain.AdherentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AdherentService implements AdherentApi {

    private final AdherentRepository adherentRepository;

    public AdherentService(AdherentRepository adherentRepository) {
        this.adherentRepository = adherentRepository;
    }

    @Override
    public AdherentDto inscrire(String nom, TypeAdherent type) {
        String id = "ADH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Adherent adherent = adherentRepository.save(new Adherent(id, nom, type));
        return toDto(adherent);
    }

    @Override
    @Transactional(readOnly = true)
    public AdherentDto trouver(String adherentId) {
        return toDto(adherentRepository.findById(adherentId)
                .orElseThrow(() -> new AdherentIntrouvableException(adherentId)));
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierEligibiliteEmprunt(String adherentId, long empruntsEnCours) {
        adherentRepository.findById(adherentId)
                .orElseThrow(() -> new AdherentIntrouvableException(adherentId))
                .verifierEligibiliteEmprunt(empruntsEnCours);
    }

    /** Appelé par l'écouteur d'événements : met à jour le drapeau de blocage de l'adhérent concerné. */
    void mettreAJourImpayes(String adherentId, boolean aDesImpayes, long montantTotalImpaye) {
        adherentRepository.findByIdPourModification(adherentId).ifPresent(adherent -> {
            adherent.mettreAJourImpayes(aDesImpayes, montantTotalImpaye);
            adherentRepository.save(adherent);
        });
    }

    private AdherentDto toDto(Adherent adherent) {
        return new AdherentDto(adherent.getId(), adherent.getNom(), adherent.getType(),
                adherent.getType().quotaEmprunts(), adherent.isEstBloquePourImpayes(), adherent.getMontantImpaye());
    }
}
