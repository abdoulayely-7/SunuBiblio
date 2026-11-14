package lydevtech.sunubiblio;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/** Petit client HTTP de test : il ne connait que les URL et le JSON de l'API, jamais le code des modules. */
final class ApiClient {

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
}
