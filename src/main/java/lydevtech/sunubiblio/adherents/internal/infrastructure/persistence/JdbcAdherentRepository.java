package lydevtech.sunubiblio.adherents.internal.infrastructure.persistence;

import lydevtech.sunubiblio.adherents.TypeAdherent;
import lydevtech.sunubiblio.adherents.internal.domain.Adherent;
import lydevtech.sunubiblio.adherents.internal.domain.AdherentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcAdherentRepository implements AdherentRepository {

    private static final RowMapper<Adherent> MAPPER = (rs, rowNum) -> new Adherent(
            rs.getObject("id", UUID.class), rs.getString("nom"), TypeAdherent.valueOf(rs.getString("type")));

    private final JdbcTemplate jdbc;

    JdbcAdherentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void ajouter(Adherent adherent) {
        jdbc.update("INSERT INTO adherents_adherent (id, nom, type) VALUES (?, ?, ?)",
                adherent.id(), adherent.nom(), adherent.type().name());
    }

    @Override
    public Optional<Adherent> trouver(UUID id) {
        return jdbc.query("SELECT id, nom, type FROM adherents_adherent WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }
}
