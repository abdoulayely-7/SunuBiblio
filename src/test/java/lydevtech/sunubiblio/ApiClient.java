package lydevtech.sunubiblio;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

/** Petit client HTTP de test : il ne connait que les URL et le JSON de l'API, jamais le code des modules. */
final class ApiClient {

    /** Un livre cree pour un test : son ISBN et les identifiants de ses exemplaires. */
    record LivreCree(String isbn, List<String> exemplaires) {
    }

    private final MockMvc mvc;

    ApiClient(MockMvc mvc) {
        this.mvc = mvc;
    }

    ResultActions get(String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.get(url));
    }

    ResultActions post(String url, String json) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions post(String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(url));
    }

    ResultActions delete(String url) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.delete(url));
    }

    static <T> T lire(ResultActions reponse, String chemin) throws Exception {
        return JsonPath.read(reponse.andReturn().getResponse().getContentAsString(), chemin);
    }

    /** Cree un livre a l'ISBN unique, avec le nombre d'exemplaires demande. */
    LivreCree nouveauLivre(int nombreExemplaires) throws Exception {
        String isbn = "978-" + UUID.randomUUID().toString().substring(0, 8);
        var reponse = post("/api/catalogue/livres", "{\"isbn\": \"" + isbn + "\", \"titre\": \"Livre " + isbn
                + "\", \"auteur\": \"Auteur\", \"nombreExemplaires\": " + nombreExemplaires + "}");
        return new LivreCree(isbn, lire(reponse, "$.exemplaires[*].id"));
    }

    /** Inscrit un adherent (ETUDIANT ou ENSEIGNANT) et renvoie son identifiant. */
    String inscrire(String type) throws Exception {
        return lire(post("/api/adherents", "{\"nom\": \"Adherent\", \"type\": \"" + type + "\"}"), "$.id");
    }

    static String corpsEmprunt(String adherentId, String exemplaireId) {
        return "{\"adherentId\": \"" + adherentId + "\", \"exemplaireId\": \"" + exemplaireId + "\"}";
    }
}
