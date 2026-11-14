package lydevtech.sunubiblio.emprunts.internal.infrastructure.persistence;

import lydevtech.sunubiblio.emprunts.internal.domain.BlocageRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class JdbcBlocageRepository implements BlocageRepository {

    private final JdbcTemplate jdbc;

    JdbcBlocageRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void bloquer(UUID penaliteId, UUID adherentId) {
        debloquer(penaliteId);
        jdbc.update("INSERT INTO emprunts_blocage (penalite_id, adherent_id) VALUES (?, ?)", penaliteId, adherentId);
    }

    @Override
    public void debloquer(UUID penaliteId) {
        jdbc.update("DELETE FROM emprunts_blocage WHERE penalite_id = ?", penaliteId);
    }

    @Override
    public boolean adherentBloque(UUID adherentId) {
        Integer nombre = jdbc.queryForObject("SELECT COUNT(*) FROM emprunts_blocage WHERE adherent_id = ?",
                Integer.class, adherentId);
        return nombre != null && nombre > 0;
    }
}
