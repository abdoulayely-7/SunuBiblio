package lydevtech.sunubiblio.adherents;

import java.util.UUID;

public interface AdherentsApi {

    AdherentDto inscrire(String nom, TypeAdherent type);

    AdherentDto trouver(UUID adherentId);
}
