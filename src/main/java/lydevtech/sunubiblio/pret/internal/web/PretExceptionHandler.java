package lydevtech.sunubiblio.pret.internal.web;

import lydevtech.sunubiblio.adherent.AdherentBloqueException;
import lydevtech.sunubiblio.adherent.AdherentIntrouvableException;
import lydevtech.sunubiblio.adherent.QuotaEmpruntsAtteintException;
import lydevtech.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import lydevtech.sunubiblio.catalogue.ExemplaireIndisponibleException;
import lydevtech.sunubiblio.catalogue.ExemplaireIntrouvableException;
import lydevtech.sunubiblio.pret.DateRetourInvalideException;
import lydevtech.sunubiblio.pret.EmpruntDejaRestitueException;
import lydevtech.sunubiblio.pret.EmpruntIntrouvableException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = PretController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
class PretExceptionHandler {

    @ExceptionHandler({EmpruntIntrouvableException.class, AdherentIntrouvableException.class,
            ExemplaireIntrouvableException.class})
    ProblemDetail introuvable(RuntimeException e) {
        return probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", e);
    }

    @ExceptionHandler({QuotaEmpruntsAtteintException.class, AdherentBloqueException.class})
    ProblemDetail empruntRefuse(RuntimeException e) {
        return probleme(HttpStatus.CONFLICT, "Emprunt refusé", e);
    }

    @ExceptionHandler({ExemplaireIndisponibleException.class, ExemplaireEtatInvalideException.class,
            EmpruntDejaRestitueException.class})
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
