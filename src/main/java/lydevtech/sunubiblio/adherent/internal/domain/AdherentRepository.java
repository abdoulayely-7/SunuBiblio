package lydevtech.sunubiblio.adherent.internal.domain;

import java.util.Optional;

public interface AdherentRepository {

    Adherent save(Adherent adherent);

    Optional<Adherent> findById(String id);

    /** Comme {@link #findById}, en verrouillant l'adhérent : deux mises à jour simultanées ne se croisent pas. */
    Optional<Adherent> findByIdPourModification(String id);
}
