package lydevtech.sunubiblio.adherents.internal.infrastructure.web;

import lydevtech.sunubiblio.adherents.AdherentIntrouvableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdherentsController.class)
class AdherentsExceptionHandler {

    @ExceptionHandler(AdherentIntrouvableException.class)
    ProblemDetail introuvable(AdherentIntrouvableException e) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", e);
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
