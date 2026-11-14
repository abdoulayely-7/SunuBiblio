package lydevtech.sunubiblio.catalogue.internal.domain;

import java.util.List;
import java.util.Optional;

public interface LivreRepository {

    Livre save(Livre livre);

    Optional<Livre> findByIsbn(String isbn);

    Optional<Livre> findByExemplaireId(String exemplaireId);

    List<Livre> findAll();
}

