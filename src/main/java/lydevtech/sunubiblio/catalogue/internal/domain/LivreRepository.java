package lydevtech.sunubiblio.catalogue.internal.domain;

import java.util.List;
import java.util.Optional;

public interface LivreRepository {

    Livre save(Livre livre);

    Optional<Livre> findByIsbn(String isbn);

    Optional<Livre> findByExemplaireId(String exemplaireId);

    /** Comme {@link #findByExemplaireId}, en verrouillant l'exemplaire : deux emprunts simultanés ne se croisent pas. */
    Optional<Livre> findByExemplaireIdPourModification(String exemplaireId);

    List<Livre> findAll();

    List<Livre> rechercher(String texte);
}
