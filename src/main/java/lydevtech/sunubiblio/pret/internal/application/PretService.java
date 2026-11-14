package lydevtech.sunubiblio.pret.internal.application;

import lydevtech.sunubiblio.adherent.AdherentApi;
import lydevtech.sunubiblio.catalogue.CatalogueApi;
import lydevtech.sunubiblio.penalite.PenaliteApi;
import lydevtech.sunubiblio.penalite.PenaliteDto;
import lydevtech.sunubiblio.pret.EmpruntDejaRestitueException;
import lydevtech.sunubiblio.pret.EmpruntDto;
import lydevtech.sunubiblio.pret.EmpruntIntrouvableException;
import lydevtech.sunubiblio.pret.PretApi;
import lydevtech.sunubiblio.pret.RestitutionDto;
import lydevtech.sunubiblio.pret.internal.domain.Emprunt;
import lydevtech.sunubiblio.pret.internal.domain.EmpruntRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Le prêt orchestre : c'est le seul module qui appelle les trois autres, dans une seule transaction. */
@Service
@Transactional
public class PretService implements PretApi {

    private final AdherentApi adherentApi;
    private final CatalogueApi catalogueApi;
    private final PenaliteApi penaliteApi;
    private final EmpruntRepository empruntRepository;
    private final Clock horloge;

    public PretService(AdherentApi adherentApi, CatalogueApi catalogueApi, PenaliteApi penaliteApi,
                       EmpruntRepository empruntRepository, Clock horloge) {
        this.adherentApi = adherentApi;
        this.catalogueApi = catalogueApi;
        this.penaliteApi = penaliteApi;
        this.empruntRepository = empruntRepository;
        this.horloge = horloge;
    }

    @Override
    public EmpruntDto emprunter(String adherentId, String exemplaireId) {
        adherentApi.verifierEligibiliteEmprunt(adherentId, empruntRepository.compterEnCoursDe(adherentId));
        catalogueApi.verifierDisponibilite(exemplaireId);
        catalogueApi.marquerEmprunte(exemplaireId);

        String id = "EMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Emprunt emprunt = Emprunt.ouvrir(id, adherentId, exemplaireId, LocalDate.now(horloge));
        empruntRepository.ajouter(emprunt);
        return toDto(emprunt);
    }

    @Override
    public RestitutionDto restituer(String empruntId, LocalDate dateRetour) {
        Emprunt emprunt = charger(empruntId);
        Emprunt restitue = emprunt.restituer(dateRetour != null ? dateRetour : LocalDate.now(horloge));

        if (!empruntRepository.enregistrerRestitution(empruntId, restitue.dateRetour())) {
            throw new EmpruntDejaRestitueException(empruntId);
        }
        catalogueApi.marquerDisponible(emprunt.exemplaireId());

        int joursDeRetard = restitue.joursDeRetard();
        PenaliteDto penalite = joursDeRetard > 0
                ? penaliteApi.creerPenalite(emprunt.adherentId(), emprunt.exemplaireId(), joursDeRetard)
                : null;
        return new RestitutionDto(toDto(restitue), joursDeRetard, penalite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpruntDto> getEmpruntsEnCours(String adherentId) {
        adherentApi.trouver(adherentId);
        return empruntRepository.enCoursDe(adherentId).stream().map(PretService::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpruntDto trouver(String empruntId) {
        return toDto(charger(empruntId));
    }

    private Emprunt charger(String empruntId) {
        return empruntRepository.trouver(empruntId).orElseThrow(() -> new EmpruntIntrouvableException(empruntId));
    }

    private static EmpruntDto toDto(Emprunt e) {
        return new EmpruntDto(e.id(), e.adherentId(), e.exemplaireId(), e.dateEmprunt(), e.dateEcheance(),
                e.dateRetour(), e.statut());
    }
}
