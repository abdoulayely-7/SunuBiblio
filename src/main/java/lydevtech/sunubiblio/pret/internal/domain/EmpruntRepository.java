package lydevtech.sunubiblio.pret.internal.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmpruntRepository {

    void ajouter(Emprunt emprunt);

    Optional<Emprunt> trouver(String id);

    List<Emprunt> enCoursDe(String adherentId);

    long compterEnCoursDe(String adherentId);

    /** Enregistre la restitution seulement si l'emprunt est encore en cours ; faux sinon. */
    boolean enregistrerRestitution(String id, LocalDate dateRetour);
}
