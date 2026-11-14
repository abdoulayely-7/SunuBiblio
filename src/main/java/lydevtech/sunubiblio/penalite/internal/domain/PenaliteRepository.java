package lydevtech.sunubiblio.penalite.internal.domain;

import java.util.List;
import java.util.Optional;

public interface PenaliteRepository {

    Penalite save(Penalite penalite);

    Optional<Penalite> findById(String id);

    List<Penalite> findByAdherentId(String adherentId);

    List<Penalite> findAll();
}

