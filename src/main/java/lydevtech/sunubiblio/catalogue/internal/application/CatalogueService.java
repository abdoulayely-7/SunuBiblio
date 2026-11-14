package lydevtech.sunubiblio.catalogue.internal.application;

import lydevtech.sunubiblio.catalogue.CatalogueApi;
import lydevtech.sunubiblio.catalogue.ExemplaireDto;
import lydevtech.sunubiblio.catalogue.LivreDto;
import lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire;
import lydevtech.sunubiblio.catalogue.internal.domain.Livre;
import lydevtech.sunubiblio.catalogue.internal.domain.LivreRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CatalogueService implements CatalogueApi {

    private final LivreRepository livreRepository;

    public CatalogueService(LivreRepository livreRepository) {
        this.livreRepository = livreRepository;
    }

    @Override
    public LivreDto ajouterLivre(String isbn, String titre, String auteur, int nombreExemplaires) {
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
        Livre livre = livreRepository.findByExemplaireId(exemplaireId)
                .orElseThrow(() -> new IllegalArgumentException("Exemplaire introuvable: " + exemplaireId));

        Exemplaire exemplaire = livre.trouverExemplaire(exemplaireId)
                .orElseThrow(() -> new IllegalArgumentException("Exemplaire introuvable: " + exemplaireId));

        exemplaire.retirer();
        livreRepository.save(livre);
    }

    @Override
    public boolean estDisponible(String exemplaireId) {
        return trouverExemplaireInterne(exemplaireId)
                .map(Exemplaire::isDisponible)
                .orElse(false);
    }

    @Override
    public void marquerEmprunte(String exemplaireId) {
        Livre livre = livreRepository.findByExemplaireId(exemplaireId)
                .orElseThrow(() -> new IllegalArgumentException("Exemplaire introuvable: " + exemplaireId));

        Exemplaire exemplaire = livre.trouverExemplaire(exemplaireId)
                .orElseThrow(() -> new IllegalArgumentException("Exemplaire introuvable: " + exemplaireId));

        exemplaire.emprunter();
        livreRepository.save(livre);
    }

    @Override
    public void marquerDisponible(String exemplaireId) {
        Livre livre = livreRepository.findByExemplaireId(exemplaireId)
                .orElseThrow(() -> new IllegalArgumentException("Exemplaire introuvable: " + exemplaireId));

        Exemplaire exemplaire = livre.trouverExemplaire(exemplaireId)
                .orElseThrow(() -> new IllegalArgumentException("Exemplaire introuvable: " + exemplaireId));

        exemplaire.rendre();
        livreRepository.save(livre);
    }

    @Override
    public Optional<ExemplaireDto> trouverExemplaire(String exemplaireId) {
        return trouverExemplaireInterne(exemplaireId)
                .map(this::toDto);
    }

    @Override
    public Optional<LivreDto> trouverLivreParIsbn(String isbn) {
        return livreRepository.findByIsbn(isbn).map(this::toDto);
    }

    @Override
    public List<LivreDto> listerLivres() {
        return livreRepository.findAll().stream().map(this::toDto).toList();
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

