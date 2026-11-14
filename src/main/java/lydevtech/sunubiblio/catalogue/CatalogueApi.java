package lydevtech.sunubiblio.catalogue;

import java.util.List;
import java.util.Optional;

public interface CatalogueApi {

    /** Ajoute des exemplaires à un livre ; si l'ISBN est déjà au catalogue, les exemplaires s'ajoutent à ce livre. */
    LivreDto ajouterLivre(String isbn, String titre, String auteur, int nombreExemplaires);

    void retirerExemplaire(String exemplaireId);

    boolean estDisponible(String exemplaireId);

    /** Refuse, avec une explication, un exemplaire inconnu ou qui n'est pas disponible. */
    void verifierDisponibilite(String exemplaireId);

    void marquerEmprunte(String exemplaireId);

    void marquerDisponible(String exemplaireId);

    Optional<ExemplaireDto> trouverExemplaire(String exemplaireId);

    Optional<LivreDto> trouverLivreParIsbn(String isbn);

    List<LivreDto> listerLivres();

    /** Livres dont le titre, l'auteur ou l'ISBN contient le texte (sans tenir compte des majuscules). */
    List<LivreDto> rechercherLivres(String texte);
}
