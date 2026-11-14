package lydevtech.sunubiblio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class BaseDeTestTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void les_tests_tournent_sur_h2_en_memoire_et_jamais_sur_la_vraie_base() throws Exception {
        try (Connection connexion = dataSource.getConnection()) {
            String url = connexion.getMetaData().getURL();

            assertTrue(url.startsWith("jdbc:h2:mem:"), "les tests doivent utiliser H2 en memoire, url : " + url);
        }
    }
}
