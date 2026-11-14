package lydevtech.sunubiblio.catalogue;

import java.util.List;
import java.util.UUID;

public interface CatalogueApi {

    LivreDto ajouterLivre(String titre, String auteur, int nombreExemplaires);

    ExemplaireDto ajouterExemplaire(UUID livreId);

    void retirerExemplaire(UUID exemplaireId);

    LivreDto trouverLivre(UUID livreId);

    List<LivreDto> rechercher(String texte);

    void marquerEmprunte(UUID exemplaireId);

    void marquerDisponible(UUID exemplaireId);
}
