package lydevtech.sunubiblio;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contrat OpenAPI de l'API. Les reponses d'erreur sont definies une seule fois ici ;
 * les controleurs y renvoient avec {@code @ApiResponse(responseCode = "...", ref = "...")}, et les erreurs
 * 500 et 503, possibles partout, sont ajoutees a chaque operation par {@link #erreursPartout()}.
 */
@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI contrat() {
        Components composants = new Components()
                .addSchemas("ProblemDetail", problemDetail())
                .addSchemas("ErreurChamp", erreurChamp())
                .addResponses("RequeteInvalide", reponseErreur("Requête invalide : corps absent ou illisible, champ manquant ou incorrect, identifiant mal formé. Le détail dit quel champ corriger ; `erreurs` liste chaque champ fautif."))
                .addResponses("Introuvable", reponseErreur("Ressource introuvable."))
                .addResponses("Conflit", reponseErreur("Règle du métier refusée (quota, pénalité impayée, exemplaire indisponible...)."))
                .addResponses("ErreurInterne", reponseErreur("Erreur inattendue du serveur. Le détail contient un identifiant de suivi (`traceId`) à communiquer à l'équipe technique."))
                .addResponses("ServiceIndisponible", reponseErreur("Base de données momentanément indisponible : réessayer dans quelques instants (en-tête Retry-After)."));

        return new OpenAPI()
                .info(new Info()
                        .title("SunuBiblio")
                        .version("0.0.1")
                        .description("Prêts de la bibliothèque universitaire : livres et exemplaires, adhérents, "
                                + "emprunts, retours et pénalités de retard. Toutes les erreurs suivent le format "
                                + "Problem Details (RFC 9457), avec un message en français explicite."))
                .addServersItem(new Server().url("http://localhost:8080").description("Serveur local"))
                .components(composants);
    }

    @Bean
    OpenApiCustomizer erreursPartout() {
        return openApi -> openApi.getPaths().values().forEach(chemin -> chemin.readOperations().forEach(operation -> {
            operation.getResponses().addApiResponse("500", new ApiResponse().$ref("ErreurInterne"));
            operation.getResponses().addApiResponse("503", new ApiResponse().$ref("ServiceIndisponible"));
        }));
    }

    private static Schema<?> problemDetail() {
        return new ObjectSchema()
                .description("Erreur au format Problem Details (RFC 9457), sans détail interne.")
                .addProperty("type", new StringSchema().example("about:blank"))
                .addProperty("title", new StringSchema().example("Emprunt refusé"))
                .addProperty("status", new IntegerSchema().example(409))
                .addProperty("detail", new StringSchema().example("L'adhérent a une pénalité de retard impayée : il doit d'abord la payer avant de pouvoir emprunter de nouveau."))
                .addProperty("instance", new StringSchema().example("/emprunts"))
                .addProperty("erreurs", new ArraySchema().items(new Schema<>().$ref("#/components/schemas/ErreurChamp"))
                        .description("Présent pour les erreurs de validation : un élément par champ fautif."))
                .addProperty("traceId", new StringSchema().example("3f9a1c2e")
                        .description("Présent pour les erreurs 500 et 503 : identifiant à communiquer à l'équipe technique."));
    }

    private static Schema<?> erreurChamp() {
        return new ObjectSchema()
                .addProperty("champ", new StringSchema().example("titre"))
                .addProperty("message", new StringSchema().example("Le titre du livre est obligatoire."));
    }

    private static ApiResponse reponseErreur(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/problem+json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))));
    }
}
