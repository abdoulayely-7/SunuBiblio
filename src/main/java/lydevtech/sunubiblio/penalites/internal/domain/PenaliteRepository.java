package lydevtech.sunubiblio.penalites.internal.domain;

import lydevtech.sunubiblio.penalites.StatutPenalite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PenaliteRepository {

    void ajouter(Penalite penalite);

    Optional<Penalite> trouver(UUID id);

    List<Penalite> parAdherent(UUID adherentId);

    boolean existeImpayee(UUID adherentId);

    /** Passe de {@code attendu} a {@code nouveau} seulement si le statut n'a pas change entre-temps. */
    boolean changerStatut(UUID id, StatutPenalite attendu, StatutPenalite nouveau);
}
