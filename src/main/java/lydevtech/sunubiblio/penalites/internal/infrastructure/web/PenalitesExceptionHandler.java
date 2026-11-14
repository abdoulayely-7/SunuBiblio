package lydevtech.sunubiblio.penalites.internal.infrastructure.web;

import lydevtech.sunubiblio.penalites.PenaliteDejaPayeeException;
import lydevtech.sunubiblio.penalites.PenaliteIntrouvableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PenalitesController.class)
class PenalitesExceptionHandler {

    @ExceptionHandler(PenaliteIntrouvableException.class)
    ProblemDetail introuvable(PenaliteIntrouvableException e) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", e);
    }

    @ExceptionHandler(PenaliteDejaPayeeException.class)
    ProblemDetail conflit(PenaliteDejaPayeeException e) {
        return probleme(HttpStatus.CONFLICT, "Opération refusée", e);
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, RuntimeException e) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, e.getMessage());
        probleme.setTitle(titre);
        return probleme;
    }
}
