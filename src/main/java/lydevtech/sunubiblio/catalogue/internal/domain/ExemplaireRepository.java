package lydevtech.sunubiblio.catalogue.internal.domain;

import lydevtech.sunubiblio.catalogue.EtatExemplaire;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExemplaireRepository {

    void ajouter(Exemplaire exemplaire);

    Optional<Exemplaire> trouver(UUID id);

    List<Exemplaire> parLivre(UUID livreId);

    /** Passe de {@code attendu} a {@code nouveau} seulement si l'etat n'a pas change entre-temps. */
    boolean changerEtat(UUID id, EtatExemplaire attendu, EtatExemplaire nouveau);
}
