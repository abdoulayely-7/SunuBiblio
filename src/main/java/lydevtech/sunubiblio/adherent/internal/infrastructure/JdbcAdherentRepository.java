package lydevtech.sunubiblio.adherent.internal.infrastructure;

import lydevtech.sunubiblio.adherent.TypeAdherent;
import lydevtech.sunubiblio.adherent.internal.domain.Adherent;
import lydevtech.sunubiblio.adherent.internal.domain.AdherentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class JdbcAdherentRepository implements AdherentRepository {

    private static final String COLONNES = "id, nom, type, est_bloque_pour_impayes, montant_impaye";

    private static final RowMapper<Adherent> MAPPER = (rs, ligne) -> Adherent.restaurer(
            rs.getString("id"),
            rs.getString("nom"),
            TypeAdherent.valueOf(rs.getString("type")),
            rs.getBoolean("est_bloque_pour_impayes"),
            rs.getLong("montant_impaye"));

    private final JdbcTemplate jdbc;

    JdbcAdherentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Adherent save(Adherent a) {
        int modifie = jdbc.update("UPDATE adherent_adherent SET est_bloque_pour_impayes = ?, montant_impaye = ? WHERE id = ?",
                a.isEstBloquePourImpayes(), a.getMontantImpaye(), a.getId());
        if (modifie == 0) {
            jdbc.update("INSERT INTO adherent_adherent (" + COLONNES + ") VALUES (?, ?, ?, ?, ?)",
                    a.getId(), a.getNom(), a.getType().name(), a.isEstBloquePourImpayes(), a.getMontantImpaye());
        }
        return a;
    }

    @Override
    public Optional<Adherent> findById(String id) {
        return jdbc.query("SELECT " + COLONNES + " FROM adherent_adherent WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<Adherent> findByIdPourModification(String id) {
        return jdbc.query("SELECT " + COLONNES + " FROM adherent_adherent WHERE id = ? FOR UPDATE", MAPPER, id)
                .stream().findFirst();
    }
}
