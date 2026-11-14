package lydevtech.sunubiblio.catalogue.internal.infrastructure;

import lydevtech.sunubiblio.catalogue.StatutExemplaire;
import lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire;
import lydevtech.sunubiblio.catalogue.internal.domain.Livre;
import lydevtech.sunubiblio.catalogue.internal.domain.LivreRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Persistance PostgreSQL du catalogue : remplace le dépôt en mémoire, derrière la même interface. */
@Repository
class JdbcLivreRepository implements LivreRepository {

    private final JdbcTemplate jdbc;

    JdbcLivreRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Livre save(Livre livre) {
        int misAJour = jdbc.update("UPDATE catalogue_livre SET titre = ?, auteur = ? WHERE isbn = ?",
                livre.getTitre(), livre.getAuteur(), livre.getIsbn());
        if (misAJour == 0) {
            jdbc.update("INSERT INTO catalogue_livre (isbn, titre, auteur) VALUES (?, ?, ?)",
                    livre.getIsbn(), livre.getTitre(), livre.getAuteur());
        }
        for (Exemplaire exemplaire : livre.getExemplaires()) {
            int modifie = jdbc.update("UPDATE catalogue_exemplaire SET statut = ? WHERE id = ?",
                    exemplaire.getStatut().name(), exemplaire.getId());
            if (modifie == 0) {
                jdbc.update("INSERT INTO catalogue_exemplaire (id, livre_isbn, statut) VALUES (?, ?, ?)",
                        exemplaire.getId(), exemplaire.getLivreIsbn(), exemplaire.getStatut().name());
            }
        }
        return livre;
    }

    @Override
    public Optional<Livre> findByIsbn(String isbn) {
        return charger(isbn);
    }

    @Override
    public Optional<Livre> findByExemplaireId(String exemplaireId) {
        return isbnDe("SELECT livre_isbn FROM catalogue_exemplaire WHERE id = ?", exemplaireId).flatMap(this::charger);
    }

    @Override
    public Optional<Livre> findByExemplaireIdPourModification(String exemplaireId) {
        return isbnDe("SELECT livre_isbn FROM catalogue_exemplaire WHERE id = ? FOR UPDATE", exemplaireId)
                .flatMap(this::charger);
    }

    @Override
    public List<Livre> findAll() {
        return jdbc.query("SELECT isbn, titre, auteur FROM catalogue_livre ORDER BY titre, isbn",
                (rs, ligne) -> new Livre(rs.getString("isbn"), rs.getString("titre"), rs.getString("auteur")))
                .stream().map(this::avecExemplaires).toList();
    }

    @Override
    public List<Livre> rechercher(String texte) {
        // % et _ saisis par l'utilisateur sont du texte à chercher, pas des jokers SQL : on les échappe.
        String echappe = texte.toLowerCase().replace("!", "!!").replace("%", "!%").replace("_", "!_");
        String motif = "%" + echappe + "%";
        return jdbc.query("SELECT isbn, titre, auteur FROM catalogue_livre "
                        + "WHERE LOWER(titre) LIKE ? ESCAPE '!' OR LOWER(auteur) LIKE ? ESCAPE '!' "
                        + "OR LOWER(isbn) LIKE ? ESCAPE '!' ORDER BY titre, isbn",
                        (rs, ligne) -> new Livre(rs.getString("isbn"), rs.getString("titre"), rs.getString("auteur")),
                        motif, motif, motif)
                .stream().map(this::avecExemplaires).toList();
    }

    private Optional<String> isbnDe(String sql, String exemplaireId) {
        return jdbc.queryForList(sql, String.class, exemplaireId).stream().findFirst();
    }

    private Optional<Livre> charger(String isbn) {
        return jdbc.query("SELECT isbn, titre, auteur FROM catalogue_livre WHERE isbn = ?",
                        (rs, ligne) -> new Livre(rs.getString("isbn"), rs.getString("titre"), rs.getString("auteur")), isbn)
                .stream().findFirst().map(this::avecExemplaires);
    }

    private Livre avecExemplaires(Livre livre) {
        jdbc.query("SELECT id, statut FROM catalogue_exemplaire WHERE livre_isbn = ? ORDER BY id",
                (RowCallbackHandler) rs -> livre.restaurerExemplaire(rs.getString("id"),
                        StatutExemplaire.valueOf(rs.getString("statut"))),
                livre.getIsbn());
        return livre;
    }
}
