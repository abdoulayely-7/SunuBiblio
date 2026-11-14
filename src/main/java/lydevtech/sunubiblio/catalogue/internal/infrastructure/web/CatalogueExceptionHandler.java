package lydevtech.sunubiblio.catalogue.internal.infrastructure.web;

import lydevtech.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import lydevtech.sunubiblio.catalogue.ExemplaireIndisponibleException;
import lydevtech.sunubiblio.catalogue.ExemplaireIntrouvableException;
import lydevtech.sunubiblio.catalogue.LivreIntrouvableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = CatalogueController.class)
class CatalogueExceptionHandler {

    @ExceptionHandler({LivreIntrouvableException.class, ExemplaireIntrouvableException.class})
    ProblemDetail introuvable(RuntimeException e) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", e);
    }

    @ExceptionHandler({ExemplaireIndisponibleException.class, ExemplaireEtatInvalideException.class})
    ProblemDetail conflit(RuntimeException e) {
        return probleme(HttpStatus.CONFLICT, "Opération refusée", e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail requeteInvalide(IllegalArgumentException e) {
        return probleme(HttpStatus.BAD_REQUEST, "Requête invalide", e);
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, RuntimeException e) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, e.getMessage());
        probleme.setTitle(titre);
        return probleme;
    }
}
