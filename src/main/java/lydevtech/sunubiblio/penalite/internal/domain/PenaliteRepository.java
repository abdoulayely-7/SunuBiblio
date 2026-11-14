package lydevtech.sunubiblio.penalite.internal.domain;

import java.util.List;
import java.util.Optional;

public interface PenaliteRepository {

    Penalite save(Penalite penalite);

    Optional<Penalite> findById(String id);

    /** Comme {@link #findById}, en verrouillant la pénalité : deux paiements simultanés ne se croisent pas. */
    Optional<Penalite> findByIdPourModification(String id);

    List<Penalite> findByAdherentId(String adherentId);

    List<Penalite> findAll();
}
