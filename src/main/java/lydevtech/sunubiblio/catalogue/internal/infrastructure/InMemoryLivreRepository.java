package lydevtech.sunubiblio.catalogue.internal.infrastructure;

import lydevtech.sunubiblio.catalogue.internal.domain.Livre;
import lydevtech.sunubiblio.catalogue.internal.domain.LivreRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryLivreRepository implements LivreRepository {

    private final Map<String, Livre> livresByIsbn = new ConcurrentHashMap<>();

    @Override
    public Livre save(Livre livre) {
        livresByIsbn.put(livre.getIsbn(), livre);
        return livre;
    }

    @Override
    public Optional<Livre> findByIsbn(String isbn) {
        return Optional.ofNullable(livresByIsbn.get(isbn));
    }

    @Override
    public Optional<Livre> findByExemplaireId(String exemplaireId) {
        return livresByIsbn.values().stream()
                .filter(livre -> livre.trouverExemplaire(exemplaireId).isPresent())
                .findFirst();
    }

    @Override
    public List<Livre> findAll() {
        return new ArrayList<>(livresByIsbn.values());
    }
}

