package lydevtech.sunubiblio;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Contrat OpenAPI de l'API. Les reponses d'erreur sont definies une seule fois ici ;
 * les controleurs y renvoient avec {@code @ApiResponse(responseCode = "...", ref = "...")}.
 */
@Configuration
class OpenApiConfiguration {

    @Bean
    OpenAPI contrat() {
        Components composants = new Components()
                .addSchemas("ProblemDetail", problemDetail())
                .addResponses("RequeteInvalide", reponseErreur("Requête invalide ou incomplète (champ manquant, valeur incorrecte)."))
                .addResponses("Introuvable", reponseErreur("Ressource introuvable."))
                .addResponses("Conflit", reponseErreur("Règle du métier refusée (quota, pénalité impayée, exemplaire indisponible...)."));

        return new OpenAPI()
                .info(new Info()
                        .title("SunuBiblio")
                        .version("0.0.1")
                        .description("Prêts de la bibliothèque universitaire : livres et exemplaires, adhérents, "
                                + "emprunts, retours et pénalités de retard. Toutes les erreurs suivent le format "
                                + "Problem Details (RFC 9457)."))
                .addServersItem(new Server().url("http://localhost:8080").description("Serveur local"))
                .components(composants);
    }

    private static Schema<?> problemDetail() {
        return new ObjectSchema()
                .description("Erreur au format Problem Details (RFC 9457), sans détail interne.")
                .addProperty("type", new StringSchema().example("about:blank"))
                .addProperty("title", new StringSchema().example("Opération refusée"))
                .addProperty("status", new IntegerSchema().example(409))
                .addProperty("detail", new StringSchema().example("L'adhérent a une pénalité impayée : il ne peut plus emprunter"))
                .addProperty("instance", new StringSchema().example("/emprunts"));
    }

    private static ApiResponse reponseErreur(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/problem+json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ProblemDetail"))));
    }
}
