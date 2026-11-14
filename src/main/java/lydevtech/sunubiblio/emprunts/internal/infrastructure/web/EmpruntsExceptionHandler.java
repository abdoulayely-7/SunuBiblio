package lydevtech.sunubiblio.emprunts.internal.infrastructure.web;

import lydevtech.sunubiblio.adherents.AdherentIntrouvableException;
import lydevtech.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import lydevtech.sunubiblio.catalogue.ExemplaireIndisponibleException;
import lydevtech.sunubiblio.catalogue.ExemplaireIntrouvableException;
import lydevtech.sunubiblio.emprunts.DateRetourInvalideException;
import lydevtech.sunubiblio.emprunts.EmpruntDejaRenduException;
import lydevtech.sunubiblio.emprunts.EmpruntIntrouvableException;
import lydevtech.sunubiblio.emprunts.PenaliteImpayeeException;
import lydevtech.sunubiblio.emprunts.QuotaEmpruntsAtteintException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmpruntsController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class EmpruntsExceptionHandler {

    @ExceptionHandler({EmpruntIntrouvableException.class, AdherentIntrouvableException.class,
            ExemplaireIntrouvableException.class})
    ProblemDetail introuvable(RuntimeException e) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", e);
    }

    @ExceptionHandler({QuotaEmpruntsAtteintException.class, PenaliteImpayeeException.class})
    ProblemDetail empruntRefuse(RuntimeException e) {
        return probleme(HttpStatus.CONFLICT, "Emprunt refusé", e);
    }

    @ExceptionHandler({ExemplaireIndisponibleException.class, ExemplaireEtatInvalideException.class,
            EmpruntDejaRenduException.class})
    ProblemDetail conflit(RuntimeException e) {
        return probleme(HttpStatus.CONFLICT, "Opération refusée", e);
    }


    @ExceptionHandler(DateRetourInvalideException.class)
    ProblemDetail requeteInvalide(DateRetourInvalideException e) {
        return probleme(HttpStatus.BAD_REQUEST, "Requête invalide", e);
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, RuntimeException e) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, e.getMessage());
        probleme.setTitle(titre);
        return probleme;
    }
}
