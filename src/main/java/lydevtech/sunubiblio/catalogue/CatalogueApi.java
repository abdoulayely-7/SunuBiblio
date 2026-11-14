package lydevtech.sunubiblio.catalogue;

import java.util.List;
import java.util.Optional;

public interface CatalogueApi {

    LivreDto ajouterLivre(String isbn, String titre, String auteur, int nombreExemplaires);

    void retirerExemplaire(String exemplaireId);

    boolean estDisponible(String exemplaireId);

    void marquerEmprunte(String exemplaireId);

    void marquerDisponible(String exemplaireId);

    Optional<ExemplaireDto> trouverExemplaire(String exemplaireId);

    Optional<LivreDto> trouverLivreParIsbn(String isbn);

    List<LivreDto> listerLivres();
}
