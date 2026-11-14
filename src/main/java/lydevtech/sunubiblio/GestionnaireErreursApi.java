package lydevtech.sunubiblio;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.exc.InvalidFormatException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Toutes les erreurs de l'API sortent au meme format (Problem Details, RFC 9457), avec un message en
 * francais qui dit ce qui ne va pas et comment corriger, sans jamais exposer de detail interne.
 * Les erreurs du metier sont traitees par le gestionnaire de chaque module (prioritaire) ; celui-ci
 * couvre tout le reste : requetes mal formees, routes inconnues, base indisponible, erreurs inattendues.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class GestionnaireErreursApi extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GestionnaireErreursApi.class);
    private static final Pattern CHAMP_JSON = Pattern.compile("\\[\"([^\"]+)\"\\]");
    private static final String EXEMPLE_UUID = "3fa85f64-5717-4562-b3fc-2c963f66afa6";

    /** Un champ de la requete qui ne respecte pas les contraintes. */
    record ErreurChamp(String champ, String message) {
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ErreurChamp> erreurs = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErreurChamp(e.getField(),
                        e.getDefaultMessage() == null ? "La valeur de ce champ n'est pas valide." : e.getDefaultMessage()))
                .sorted(Comparator.comparing(ErreurChamp::champ).thenComparing(ErreurChamp::message))
                .toList();
        String detail = erreurs.size() == 1
                ? erreurs.get(0).message()
                : "La requête contient " + erreurs.size() + " erreurs : "
                + erreurs.stream().map(ErreurChamp::message).collect(Collectors.joining(" ; ")) + ".";
        ProblemDetail probleme = probleme(HttpStatus.BAD_REQUEST, "Requête invalide", detail);
        probleme.setProperty("erreurs", erreurs);
        return handleExceptionInternal(ex, probleme, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail probleme = probleme(HttpStatus.BAD_REQUEST, "Requête invalide", detailCorpsIllisible(ex));
        return handleExceptionInternal(ex, probleme, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String detail = "Le paramètre '" + ex.getPropertyName() + "' n'est pas valide : "
                + attendu(ex.getRequiredType()) + " (valeur reçue : '" + ex.getValue() + "').";
        return handleExceptionInternal(ex, probleme(HttpStatus.BAD_REQUEST, "Requête invalide", detail),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detail = "Le paramètre '" + ex.getParameterName() + "' est obligatoire dans l'adresse de la requête.";
        return handleExceptionInternal(ex, probleme(HttpStatus.BAD_REQUEST, "Requête invalide", detail),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var autorisees = ex.getSupportedHttpMethods();
        String detail = "La méthode " + ex.getMethod() + " n'est pas autorisée sur cette adresse"
                + (autorisees == null || autorisees.isEmpty() ? "."
                : " : utilisez " + autorisees.stream().map(Object::toString).collect(Collectors.joining(" ou ")) + ".");
        return handleExceptionInternal(ex, probleme(HttpStatus.METHOD_NOT_ALLOWED, "Méthode non autorisée", detail),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String recu = ex.getContentType() == null ? "aucun"
                : "'" + ex.getContentType().getType() + "/" + ex.getContentType().getSubtype() + "'";
        String detail = "Le type de contenu " + recu + " n'est pas pris en charge : l'API n'accepte que du JSON, "
                + "envoyez la requête avec l'en-tête Content-Type: application/json.";
        return handleExceptionInternal(ex, probleme(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Type de contenu non pris en charge", detail),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detail = "Aucun des formats demandés (en-tête Accept) n'est disponible : l'API répond en "
                + ex.getSupportedMediaTypes().stream().map(MediaType::toString).collect(Collectors.joining(" ou ")) + ".";
        return handleExceptionInternal(ex, probleme(HttpStatus.NOT_ACCEPTABLE, "Format de réponse non disponible", detail),
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(NoResourceFoundException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detail = "Aucune ressource ne correspond à " + ex.getHttpMethod() + " /" + ex.getResourcePath()
                + " : vérifiez l'adresse (la liste des routes est dans /swagger-ui.html).";
        return handleExceptionInternal(ex, probleme(HttpStatus.NOT_FOUND, "Ressource introuvable", detail),
                headers, status, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> conflitDeDonnees(DataIntegrityViolationException ex, HttpServletRequest requete) {
        LOG.warn("Conflit de donnees sur {} {} : {}", requete.getMethod(), requete.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return reponse(probleme(HttpStatus.CONFLICT, "Conflit de données",
                "L'opération est en conflit avec des données existantes (doublon ou référence inexistante) : "
                        + "vérifiez les identifiants envoyés."));
    }

    @ExceptionHandler({CannotGetJdbcConnectionException.class, DataAccessResourceFailureException.class,
            TransientDataAccessResourceException.class})
    ResponseEntity<ProblemDetail> baseIndisponible(Exception ex, HttpServletRequest requete) {
        String suivi = identifiantDeSuivi();
        LOG.error("Base de donnees indisponible [suivi={}] sur {} {}", suivi, requete.getMethod(), requete.getRequestURI(), ex);
        ProblemDetail probleme = probleme(HttpStatus.SERVICE_UNAVAILABLE, "Service indisponible",
                "La base de données est momentanément indisponible : réessayez dans quelques instants. "
                        + "Si le problème persiste, communiquez l'identifiant de suivi " + suivi + " à l'équipe technique.");
        probleme.setProperty("traceId", suivi);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).header(HttpHeaders.RETRY_AFTER, "5")
                .contentType(MediaType.APPLICATION_PROBLEM_JSON).body(probleme);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> erreurInterne(Exception ex, HttpServletRequest requete) {
        String suivi = identifiantDeSuivi();
        LOG.error("Erreur interne [suivi={}] sur {} {}", suivi, requete.getMethod(), requete.getRequestURI(), ex);
        ProblemDetail probleme = probleme(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne",
                "Une erreur inattendue s'est produite et n'a pas pu être traitée : réessayez, et si le problème "
                        + "persiste communiquez l'identifiant de suivi " + suivi + " à l'équipe technique.");
        probleme.setProperty("traceId", suivi);
        return reponse(probleme);
    }

    private static String detailCorpsIllisible(HttpMessageNotReadableException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause() == cause ? null : cause.getCause()) {
            if (cause instanceof InvalidFormatException format) {
                return detailFormatInvalide(format);
            }
            if (cause instanceof StreamReadException) {
                return "Le corps de la requête n'est pas un JSON valide : vérifiez les guillemets, les virgules et les accolades.";
            }
        }
        if (ex.getMessage() != null && ex.getMessage().startsWith("Required request body is missing")) {
            return "Le corps de la requête est obligatoire : envoyez un objet JSON.";
        }
        return "Le corps de la requête est illisible : envoyez un objet JSON valide, par exemple {\"champ\": \"valeur\"}.";
    }

    private static String detailFormatInvalide(InvalidFormatException ex) {
        String champ = dernierChamp(ex.getPathReference());
        Class<?> cible = ex.getTargetType();
        Object valeur = ex.getValue();
        if (cible != null && cible.isEnum()) {
            String possibles = Arrays.stream(cible.getEnumConstants()).map(Object::toString).collect(Collectors.joining(", "));
            return "Le champ '" + champ + "' n'accepte pas la valeur '" + valeur + "' : valeurs possibles : " + possibles + ".";
        }
        return "Le champ '" + champ + "' n'est pas valide : " + attendu(cible) + " (valeur reçue : '" + valeur + "').";
    }

    private static String dernierChamp(String cheminJson) {
        String champ = "?";
        if (cheminJson != null) {
            Matcher matcher = CHAMP_JSON.matcher(cheminJson);
            while (matcher.find()) {
                champ = matcher.group(1);
            }
        }
        return champ;
    }

    private static String attendu(Class<?> type) {
        if (type == null) {
            return "la valeur n'a pas le bon format";
        }
        if (UUID.class.equals(type)) {
            return "un identifiant UUID est attendu, par exemple " + EXEMPLE_UUID;
        }
        if (LocalDate.class.equals(type)) {
            return "une date valide au format AAAA-MM-JJ est attendue, par exemple 2026-03-16";
        }
        if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
            return "un nombre entier est attendu";
        }
        if (type.isEnum()) {
            return "valeurs possibles : " + Arrays.stream(type.getEnumConstants()).map(Object::toString).collect(Collectors.joining(", "));
        }
        return "une valeur de type " + type.getSimpleName() + " est attendue";
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, String detail) {
        ProblemDetail probleme = ProblemDetail.forStatusAndDetail(statut, detail);
        probleme.setTitle(titre);
        return probleme;
    }

    private static ResponseEntity<ProblemDetail> reponse(ProblemDetail probleme) {
        return ResponseEntity.status(probleme.getStatus()).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(probleme);
    }

    private static String identifiantDeSuivi() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
