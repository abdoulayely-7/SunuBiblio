package lydevtech.sunubiblio.catalogue.internal.infrastructure.persistence;

import lydevtech.sunubiblio.catalogue.EtatExemplaire;
import lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire;
import lydevtech.sunubiblio.catalogue.internal.domain.ExemplaireRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcExemplaireRepository implements ExemplaireRepository {

    private static final RowMapper<Exemplaire> MAPPER = (rs, rowNum) -> new Exemplaire(
            rs.getObject("id", UUID.class),
            rs.getObject("livre_id", UUID.class),
            EtatExemplaire.valueOf(rs.getString("etat")));

    private final JdbcTemplate jdbc;

    JdbcExemplaireRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void ajouter(Exemplaire exemplaire) {
        jdbc.update("INSERT INTO catalogue_exemplaire (id, livre_id, etat) VALUES (?, ?, ?)",
                exemplaire.id(), exemplaire.livreId(), exemplaire.etat().name());
    }

    @Override
    public Optional<Exemplaire> trouver(UUID id) {
        return jdbc.query("SELECT id, livre_id, etat FROM catalogue_exemplaire WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Exemplaire> parLivre(UUID livreId) {
        return jdbc.query("SELECT id, livre_id, etat FROM catalogue_exemplaire WHERE livre_id = ? ORDER BY id",
                MAPPER, livreId);
    }

    @Override
    public boolean changerEtat(UUID id, EtatExemplaire attendu, EtatExemplaire nouveau) {
        return jdbc.update("UPDATE catalogue_exemplaire SET etat = ? WHERE id = ? AND etat = ?",
                nouveau.name(), id, attendu.name()) > 0;
    }
}
