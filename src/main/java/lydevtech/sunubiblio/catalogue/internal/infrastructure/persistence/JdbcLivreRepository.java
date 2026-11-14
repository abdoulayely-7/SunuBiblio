package lydevtech.sunubiblio.catalogue.internal.infrastructure.persistence;

import lydevtech.sunubiblio.catalogue.internal.domain.Livre;
import lydevtech.sunubiblio.catalogue.internal.domain.LivreRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class JdbcLivreRepository implements LivreRepository {

    private static final RowMapper<Livre> MAPPER = (rs, rowNum) ->
            new Livre(rs.getObject("id", UUID.class), rs.getString("titre"), rs.getString("auteur"));

    private final JdbcTemplate jdbc;

    JdbcLivreRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void ajouter(Livre livre) {
        jdbc.update("INSERT INTO catalogue_livre (id, titre, auteur) VALUES (?, ?, ?)",
                livre.id(), livre.titre(), livre.auteur());
    }

    @Override
    public Optional<Livre> trouver(UUID id) {
        return jdbc.query("SELECT id, titre, auteur FROM catalogue_livre WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Livre> rechercher(String texte) {
        String motif = "%" + (texte == null ? "" : texte.trim().toLowerCase()) + "%";
        return jdbc.query("SELECT id, titre, auteur FROM catalogue_livre "
                + "WHERE LOWER(titre) LIKE ? OR LOWER(auteur) LIKE ? ORDER BY titre", MAPPER, motif, motif);
    }
}
