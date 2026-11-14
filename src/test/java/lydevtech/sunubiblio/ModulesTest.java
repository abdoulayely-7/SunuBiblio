package lydevtech.sunubiblio;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModulesTest {

    private final ApplicationModules modules = ApplicationModules.of(SunuBiblioApplication.class);

    @Test
    void les_frontieres_entre_modules_sont_respectees() {
        modules.verify();
    }

    @Test
    void les_cinq_modules_du_decoupage_sont_detectes() {
        Set<String> noms = modules.stream()
                .map(ApplicationModule::getIdentifier)
                .map(Object::toString)
                .collect(Collectors.toSet());

        assertEquals(Set.of("catalogue", "adherents", "penalites", "emprunts", "notifications"), noms);
    }
}
