package lydevtech.sunubiblio.adherent.internal.web;

import lydevtech.sunubiblio.adherent.AdherentIntrouvableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AdherentController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class AdherentExceptionHandler {

    @ExceptionHandler(AdherentIntrouvableException.class)
    ProblemDetail introuvable(AdherentIntrouvableException e) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        probleme.setTitle("Ressource introuvable");
        return probleme;
    }
}
