package lydevtech.sunubiblio.penalite.internal.infrastructure;

import lydevtech.sunubiblio.penalite.StatutPenalite;
import lydevtech.sunubiblio.penalite.internal.domain.Penalite;
import lydevtech.sunubiblio.penalite.internal.domain.PenaliteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/** Persistance PostgreSQL des pénalités : remplace le dépôt en mémoire, derrière la même interface. */
@Repository
class JdbcPenaliteRepository implements PenaliteRepository {

    private static final String COLONNES = "id, adherent_id, exemplaire_id, jours_de_retard, montant, statut, date_creation";

    private static final RowMapper<Penalite> MAPPER = (rs, ligne) -> Penalite.restaurer(
            rs.getString("id"),
            rs.getString("adherent_id"),
            rs.getString("exemplaire_id"),
            rs.getLong("jours_de_retard"),
            rs.getLong("montant"),
            rs.getTimestamp("date_creation").toLocalDateTime(),
            StatutPenalite.valueOf(rs.getString("statut")));

    private final JdbcTemplate jdbc;

    JdbcPenaliteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Penalite save(Penalite p) {
        int modifie = jdbc.update("UPDATE penalite_penalite SET statut = ? WHERE id = ?", p.getStatut().name(), p.getId());
        if (modifie == 0) {
            jdbc.update("INSERT INTO penalite_penalite (" + COLONNES + ") VALUES (?, ?, ?, ?, ?, ?, ?)",
                    p.getId(), p.getAdherentId(), p.getExemplaireId(), p.getJoursDeRetard(), p.getMontant(),
                    p.getStatut().name(), Timestamp.valueOf(p.getDateCreation()));
        }
        return p;
    }

    @Override
    public Optional<Penalite> findById(String id) {
        return jdbc.query("SELECT " + COLONNES + " FROM penalite_penalite WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<Penalite> findByIdPourModification(String id) {
        return jdbc.query("SELECT " + COLONNES + " FROM penalite_penalite WHERE id = ? FOR UPDATE", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Penalite> findByAdherentId(String adherentId) {
        return jdbc.query("SELECT " + COLONNES + " FROM penalite_penalite WHERE adherent_id = ? ORDER BY date_creation, id",
                MAPPER, adherentId);
    }

    @Override
    public List<Penalite> findAll() {
        return jdbc.query("SELECT " + COLONNES + " FROM penalite_penalite ORDER BY date_creation, id", MAPPER);
    }
}
