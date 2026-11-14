package com.sunubiblio.emprunts.internal.infrastructure.web;

import com.sunubiblio.adherents.AdherentIntrouvableException;
import com.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import com.sunubiblio.catalogue.ExemplaireIndisponibleException;
import com.sunubiblio.catalogue.ExemplaireIntrouvableException;
import com.sunubiblio.emprunts.EmpruntDejaRenduException;
import com.sunubiblio.emprunts.EmpruntIntrouvableException;
import com.sunubiblio.emprunts.PenaliteImpayeeException;
import com.sunubiblio.emprunts.QuotaEmpruntsAtteintException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmpruntsController.class)
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
