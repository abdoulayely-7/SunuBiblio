package lydevtech.sunubiblio.pret.internal.infrastructure;

import lydevtech.sunubiblio.pret.internal.domain.Emprunt;
import lydevtech.sunubiblio.pret.internal.domain.EmpruntRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
class JdbcEmpruntRepository implements EmpruntRepository {

    private static final String COLONNES = "id, adherent_id, exemplaire_id, date_emprunt, date_echeance, date_retour";

    private static final RowMapper<Emprunt> MAPPER = (rs, ligne) -> new Emprunt(
            rs.getString("id"),
            rs.getString("adherent_id"),
            rs.getString("exemplaire_id"),
            rs.getObject("date_emprunt", LocalDate.class),
            rs.getObject("date_echeance", LocalDate.class),
            rs.getObject("date_retour", LocalDate.class));

    private final JdbcTemplate jdbc;

    JdbcEmpruntRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void ajouter(Emprunt e) {
        jdbc.update("INSERT INTO pret_emprunt (" + COLONNES + ") VALUES (?, ?, ?, ?, ?, ?)",
                e.id(), e.adherentId(), e.exemplaireId(), e.dateEmprunt(), e.dateEcheance(), e.dateRetour());
    }

    @Override
    public Optional<Emprunt> trouver(String id) {
        return jdbc.query("SELECT " + COLONNES + " FROM pret_emprunt WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<Emprunt> enCoursDe(String adherentId) {
        return jdbc.query("SELECT " + COLONNES + " FROM pret_emprunt WHERE adherent_id = ? AND date_retour IS NULL "
                + "ORDER BY date_emprunt, id", MAPPER, adherentId);
    }

    @Override
    public long compterEnCoursDe(String adherentId) {
        Long nombre = jdbc.queryForObject("SELECT COUNT(*) FROM pret_emprunt WHERE adherent_id = ? AND date_retour IS NULL",
                Long.class, adherentId);
        return nombre == null ? 0 : nombre;
    }

    @Override
    public boolean enregistrerRestitution(String id, LocalDate dateRetour) {
        return jdbc.update("UPDATE pret_emprunt SET date_retour = ? WHERE id = ? AND date_retour IS NULL", dateRetour, id) > 0;
    }
}
