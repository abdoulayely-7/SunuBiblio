package lydevtech.sunubiblio.catalogue.internal.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LivreRepository {

    void ajouter(Livre livre);

    Optional<Livre> trouver(UUID id);

    List<Livre> rechercher(String texte);
}
