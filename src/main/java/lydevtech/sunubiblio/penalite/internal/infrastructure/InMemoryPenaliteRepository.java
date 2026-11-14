package lydevtech.sunubiblio.penalite.internal.infrastructure;

import lydevtech.sunubiblio.penalite.internal.domain.Penalite;
import lydevtech.sunubiblio.penalite.internal.domain.PenaliteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPenaliteRepository implements PenaliteRepository {

    private final Map<String, Penalite> penalites = new ConcurrentHashMap<>();

    @Override
    public Penalite save(Penalite penalite) {
        penalites.put(penalite.getId(), penalite);
        return penalite;
    }

    @Override
    public Optional<Penalite> findById(String id) {
        return Optional.ofNullable(penalites.get(id));
    }

    @Override
    public List<Penalite> findByAdherentId(String adherentId) {
        return penalites.values().stream()
                .filter(p -> p.getAdherentId().equals(adherentId))
                .toList();
    }

    @Override
    public List<Penalite> findAll() {
        return List.copyOf(penalites.values());
    }
}

