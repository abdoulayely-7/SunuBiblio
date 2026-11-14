package lydevtech.sunubiblio.penalites.internal.infrastructure.persistence;

import lydevtech.sunubiblio.penalites.StatutPenalite;
import lydevtech.sunubiblio.penalites.internal.domain.Penalite;
import lydevtech.sunubiblio.penalites.internal.domain.PenaliteRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcPenaliteRepository implements PenaliteRepository {

    private static final String COLONNES = "id, adherent_id, emprunt_id, jours_de_retard, montant_xof, statut";

    private static final RowMapper<Penalite> MAPPER = (rs, rowNum) -> new Penalite(
            rs.getObject("id", UUID.class),
            rs.getObject("adherent_id", UUID.class),
            rs.getObject("emprunt_id", UUID.class),
            rs.getInt("jours_de_retard"),
            rs.getInt("montant_xof"),
            StatutPenalite.valueOf(rs.getString("statut")));

    private final JdbcTemplate jdbc;

    JdbcPenaliteRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void ajouter(Penalite p) {
        jdbc.update("INSERT INTO penalites_penalite (" + COLONNES + ") VALUES (?, ?, ?, ?, ?, ?)",
                p.id(), p.adherentId(), p.empruntId(), p.joursDeRetard(), p.montantXof(), p.statut().name());
    }

    @Override
    public Optional<Penalite> trouver(UUID id) {
        return jdbc.query("SELECT " + COLONNES + " FROM penalites_penalite WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Penalite> parAdherent(UUID adherentId) {
        return jdbc.query("SELECT " + COLONNES + " FROM penalites_penalite WHERE adherent_id = ? "
                + "ORDER BY cree_le, id", MAPPER, adherentId);
    }

    @Override
    public boolean changerStatut(UUID id, StatutPenalite attendu, StatutPenalite nouveau) {
        return jdbc.update("UPDATE penalites_penalite SET statut = ? WHERE id = ? AND statut = ?",
                nouveau.name(), id, attendu.name()) > 0;
    }
}
