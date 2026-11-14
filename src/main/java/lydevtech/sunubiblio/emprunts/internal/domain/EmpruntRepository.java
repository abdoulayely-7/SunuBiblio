package lydevtech.sunubiblio.emprunts.internal.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpruntRepository {

    void ajouter(Emprunt emprunt);

    Optional<Emprunt> trouver(UUID id);

    List<Emprunt> enCoursDe(UUID adherentId);

    long compterEnCoursDe(UUID adherentId);

    /** Enregistre le retour seulement si l'emprunt est encore en cours ; faux sinon. */
    boolean enregistrerRetour(UUID id, LocalDate dateRetour);
}
