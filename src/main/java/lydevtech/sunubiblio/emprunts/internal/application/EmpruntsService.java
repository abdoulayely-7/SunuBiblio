package lydevtech.sunubiblio.emprunts.internal.application;

import lydevtech.sunubiblio.adherents.AdherentDto;
import lydevtech.sunubiblio.adherents.AdherentsApi;
import lydevtech.sunubiblio.catalogue.CatalogueApi;
import lydevtech.sunubiblio.emprunts.EmpruntDejaRenduException;
import lydevtech.sunubiblio.emprunts.EmpruntDto;
import lydevtech.sunubiblio.emprunts.EmpruntIntrouvableException;
import lydevtech.sunubiblio.emprunts.EmpruntsApi;
import lydevtech.sunubiblio.emprunts.RetourDto;
import lydevtech.sunubiblio.emprunts.SituationAdherentDto;
import lydevtech.sunubiblio.emprunts.internal.domain.BlocageRepository;
import lydevtech.sunubiblio.emprunts.internal.domain.Emprunt;
import lydevtech.sunubiblio.emprunts.internal.domain.EmpruntRepository;
import lydevtech.sunubiblio.emprunts.internal.domain.RegleEmprunt;
import lydevtech.sunubiblio.penalites.PenaliteDto;
import lydevtech.sunubiblio.penalites.PenalitesApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Le pret : tout passe par ici. C'est le seul module qui depend des autres. */
@Service
@Transactional
class EmpruntsService implements EmpruntsApi {

    private final AdherentsApi adherents;
    private final CatalogueApi catalogue;
    private final PenalitesApi penalites;
    private final EmpruntRepository emprunts;
    private final BlocageRepository blocages;
    private final Clock horloge;

    EmpruntsService(AdherentsApi adherents, CatalogueApi catalogue, PenalitesApi penalites,
                    EmpruntRepository emprunts, BlocageRepository blocages, Clock horloge) {
        this.adherents = adherents;
        this.catalogue = catalogue;
        this.penalites = penalites;
        this.emprunts = emprunts;
        this.blocages = blocages;
        this.horloge = horloge;
    }

    @Override
    public EmpruntDto emprunter(UUID adherentId, UUID exemplaireId) {
        AdherentDto adherent = adherents.trouver(adherentId);
        RegleEmprunt.verifier(adherentId, adherent.quotaEmprunts(),
                emprunts.compterEnCoursDe(adherentId), blocages.adherentBloque(adherentId));

        catalogue.marquerEmprunte(exemplaireId);

        Emprunt emprunt = Emprunt.ouvrir(adherentId, exemplaireId, LocalDate.now(horloge));
        emprunts.ajouter(emprunt);
        return versDto(emprunt);
    }

    @Override
    public RetourDto rendre(UUID empruntId, LocalDate dateRetour) {
        Emprunt emprunt = charger(empruntId);
        Emprunt rendu = emprunt.rendu(dateRetour != null ? dateRetour : LocalDate.now(horloge));

        if (!emprunts.enregistrerRetour(empruntId, rendu.dateRetour())) {
            throw new EmpruntDejaRenduException(empruntId);
        }
        catalogue.marquerDisponible(emprunt.exemplaireId());

        int jours = rendu.joursDeRetard();
        PenaliteDto penalite = jours > 0 ? penalites.creerPourRetard(emprunt.adherentId(), empruntId, jours) : null;
        return new RetourDto(versDto(rendu), jours, penalite);
    }

    @Override
    @Transactional(readOnly = true)
    public EmpruntDto trouver(UUID empruntId) {
        return versDto(charger(empruntId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpruntDto> empruntsEnCours(UUID adherentId) {
        return emprunts.enCoursDe(adherentId).stream().map(EmpruntsService::versDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SituationAdherentDto situation(UUID adherentId) {
        adherents.trouver(adherentId);
        return new SituationAdherentDto(adherentId, empruntsEnCours(adherentId), penalites.penalitesDe(adherentId));
    }

    private Emprunt charger(UUID empruntId) {
        return emprunts.trouver(empruntId).orElseThrow(() -> new EmpruntIntrouvableException(empruntId));
    }

    private static EmpruntDto versDto(Emprunt e) {
        return new EmpruntDto(e.id(), e.adherentId(), e.exemplaireId(), e.dateEmprunt(), e.dateEcheance(),
                e.dateRetour());
    }
}
