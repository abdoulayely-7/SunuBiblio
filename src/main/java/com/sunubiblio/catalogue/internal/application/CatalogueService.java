package com.sunubiblio.catalogue.internal.application;

import com.sunubiblio.catalogue.CatalogueApi;
import com.sunubiblio.catalogue.EtatExemplaire;
import com.sunubiblio.catalogue.ExemplaireDto;
import com.sunubiblio.catalogue.ExemplaireIndisponibleException;
import com.sunubiblio.catalogue.ExemplaireIntrouvableException;
import com.sunubiblio.catalogue.LivreDto;
import com.sunubiblio.catalogue.LivreIntrouvableException;
import com.sunubiblio.catalogue.internal.domain.Exemplaire;
import com.sunubiblio.catalogue.internal.domain.ExemplaireRepository;
import com.sunubiblio.catalogue.internal.domain.Livre;
import com.sunubiblio.catalogue.internal.domain.LivreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
class CatalogueService implements CatalogueApi {

    private final LivreRepository livres;
    private final ExemplaireRepository exemplaires;

    CatalogueService(LivreRepository livres, ExemplaireRepository exemplaires) {
        this.livres = livres;
        this.exemplaires = exemplaires;
    }

    @Override
    public LivreDto ajouterLivre(String titre, String auteur, int nombreExemplaires) {
        if (nombreExemplaires < 1) {
            throw new IllegalArgumentException("Un livre doit avoir au moins un exemplaire");
        }
        Livre livre = Livre.nouveau(titre, auteur);
        livres.ajouter(livre);
        for (int i = 0; i < nombreExemplaires; i++) {
            exemplaires.ajouter(Exemplaire.neuf(livre.id()));
        }
        return versDto(livre);
    }

    @Override
    public ExemplaireDto ajouterExemplaire(UUID livreId) {
        Livre livre = livres.trouver(livreId).orElseThrow(() -> new LivreIntrouvableException(livreId));
        Exemplaire exemplaire = Exemplaire.neuf(livre.id());
        exemplaires.ajouter(exemplaire);
        return versDto(exemplaire);
    }

    @Override
    public void retirerExemplaire(UUID exemplaireId) {
        Exemplaire exemplaire = charger(exemplaireId);
        Exemplaire retire = exemplaire.retire();
        if (!exemplaires.changerEtat(exemplaireId, exemplaire.etat(), retire.etat())) {
            throw new ExemplaireIndisponibleException(exemplaireId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LivreDto trouverLivre(UUID livreId) {
        return versDto(livres.trouver(livreId).orElseThrow(() -> new LivreIntrouvableException(livreId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LivreDto> rechercher(String texte) {
        return livres.rechercher(texte).stream().map(this::versDto).toList();
    }

    @Override
    public void marquerEmprunte(UUID exemplaireId) {
        Exemplaire exemplaire = charger(exemplaireId);
        Exemplaire emprunte = exemplaire.emprunte();
        if (!exemplaires.changerEtat(exemplaireId, exemplaire.etat(), emprunte.etat())) {
            throw new ExemplaireIndisponibleException(exemplaireId);
        }
    }

    @Override
    public void marquerDisponible(UUID exemplaireId) {
        Exemplaire exemplaire = charger(exemplaireId);
        Exemplaire rendu = exemplaire.rendu();
        exemplaires.changerEtat(exemplaireId, exemplaire.etat(), rendu.etat());
    }

    private Exemplaire charger(UUID exemplaireId) {
        return exemplaires.trouver(exemplaireId).orElseThrow(() -> new ExemplaireIntrouvableException(exemplaireId));
    }

    private LivreDto versDto(Livre livre) {
        List<ExemplaireDto> liste = exemplaires.parLivre(livre.id()).stream().map(this::versDto).toList();
        int disponibles = (int) liste.stream().filter(e -> e.etat() == EtatExemplaire.DISPONIBLE).count();
        return new LivreDto(livre.id(), livre.titre(), livre.auteur(), disponibles, liste);
    }

    private ExemplaireDto versDto(Exemplaire exemplaire) {
        return new ExemplaireDto(exemplaire.id(), exemplaire.livreId(), exemplaire.etat());
    }
}
