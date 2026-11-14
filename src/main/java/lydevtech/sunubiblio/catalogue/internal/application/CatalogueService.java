package lydevtech.sunubiblio.catalogue.internal.application;

import lydevtech.sunubiblio.catalogue.CatalogueApi;
import lydevtech.sunubiblio.catalogue.ExemplaireDto;
import lydevtech.sunubiblio.catalogue.ExemplaireIndisponibleException;
import lydevtech.sunubiblio.catalogue.ExemplaireIntrouvableException;
import lydevtech.sunubiblio.catalogue.LivreDto;
import lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire;
import lydevtech.sunubiblio.catalogue.internal.domain.Livre;
import lydevtech.sunubiblio.catalogue.internal.domain.LivreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CatalogueService implements CatalogueApi {

    private final LivreRepository livreRepository;

    public CatalogueService(LivreRepository livreRepository) {
        this.livreRepository = livreRepository;
    }

    @Override
    public LivreDto ajouterLivre(String isbn, String titre, String auteur, int nombreExemplaires) {
        if (nombreExemplaires < 1) {
            throw new IllegalArgumentException("Un livre doit avoir au moins un exemplaire.");
        }
        Livre livre = livreRepository.findByIsbn(isbn)
                .orElseGet(() -> new Livre(isbn, titre, auteur));
        for (int i = 0; i < nombreExemplaires; i++) {
            String exemplaireId = "EX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            livre.ajouterExemplaire(exemplaireId);
        }
        livreRepository.save(livre);
        return toDto(livre);
    }

    @Override
    public void retirerExemplaire(String exemplaireId) {
        Livre livre = chargerPourModification(exemplaireId);
        livre.trouverExemplaire(exemplaireId)
                .orElseThrow(() -> new ExemplaireIntrouvableException(exemplaireId))
                .retirer();
        livreRepository.save(livre);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estDisponible(String exemplaireId) {
        return trouverExemplaireInterne(exemplaireId)
                .map(Exemplaire::isDisponible)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierDisponibilite(String exemplaireId) {
        Exemplaire exemplaire = trouverExemplaireInterne(exemplaireId)
                .orElseThrow(() -> new ExemplaireIntrouvableException(exemplaireId));
        if (!exemplaire.isDisponible()) {
            throw new ExemplaireIndisponibleException(exemplaireId, exemplaire.getStatut());
        }
    }

    @Override
    public void marquerEmprunte(String exemplaireId) {
        Livre livre = chargerPourModification(exemplaireId);
        livre.trouverExemplaire(exemplaireId)
                .orElseThrow(() -> new ExemplaireIntrouvableException(exemplaireId))
                .emprunter();
        livreRepository.save(livre);
    }

    @Override
    public void marquerDisponible(String exemplaireId) {
        Livre livre = chargerPourModification(exemplaireId);
        livre.trouverExemplaire(exemplaireId)
                .orElseThrow(() -> new ExemplaireIntrouvableException(exemplaireId))
                .rendre();
        livreRepository.save(livre);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExemplaireDto> trouverExemplaire(String exemplaireId) {
        return trouverExemplaireInterne(exemplaireId)
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LivreDto> trouverLivreParIsbn(String isbn) {
        return livreRepository.findByIsbn(isbn).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LivreDto> listerLivres() {
        return livreRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LivreDto> rechercherLivres(String texte) {
        if (texte == null || texte.isBlank()) {
            return listerLivres();
        }
        return livreRepository.rechercher(texte.trim()).stream().map(this::toDto).toList();
    }

    private Livre chargerPourModification(String exemplaireId) {
        return livreRepository.findByExemplaireIdPourModification(exemplaireId)
                .orElseThrow(() -> new ExemplaireIntrouvableException(exemplaireId));
    }

    private Optional<Exemplaire> trouverExemplaireInterne(String exemplaireId) {
        return livreRepository.findByExemplaireId(exemplaireId)
                .flatMap(livre -> livre.trouverExemplaire(exemplaireId));
    }

    private LivreDto toDto(Livre livre) {
        List<ExemplaireDto> exemplaireDtos = livre.getExemplaires().stream()
                .map(this::toDto)
                .toList();
        return new LivreDto(livre.getIsbn(), livre.getTitre(), livre.getAuteur(), exemplaireDtos);
    }

    private ExemplaireDto toDto(Exemplaire exemplaire) {
        return new ExemplaireDto(exemplaire.getId(), exemplaire.getLivreIsbn(), exemplaire.getStatut());
    }
}
