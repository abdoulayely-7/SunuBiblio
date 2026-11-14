package lydevtech.sunubiblio;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulesTest {

    ApplicationModules modules = ApplicationModules.of(SunuBiblioApplication.class);

    @Test
    void verifyModularity() {
        System.out.println(modules);
        modules.verify();
    }
}

