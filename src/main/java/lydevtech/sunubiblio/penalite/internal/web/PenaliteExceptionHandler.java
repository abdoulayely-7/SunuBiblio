package lydevtech.sunubiblio.penalite.internal.web;

import lydevtech.sunubiblio.penalite.PenaliteDejaRegleeException;
import lydevtech.sunubiblio.penalite.PenaliteIntrouvableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PenaliteController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class PenaliteExceptionHandler {

    @ExceptionHandler(PenaliteIntrouvableException.class)
    ProblemDetail introuvable(PenaliteIntrouvableException e) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", e);
    }

    @ExceptionHandler(PenaliteDejaRegleeException.class)
    ProblemDetail conflit(PenaliteDejaRegleeException e) {
        return probleme(HttpStatus.CONFLICT, "Opération refusée", e);
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, RuntimeException e) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, e.getMessage());
        probleme.setTitle(titre);
        return probleme;
    }
}
