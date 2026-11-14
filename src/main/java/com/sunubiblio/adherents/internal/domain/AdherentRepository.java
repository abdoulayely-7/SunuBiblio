package com.sunubiblio.adherents.internal.domain;

import java.util.Optional;
import java.util.UUID;

public interface AdherentRepository {

    void ajouter(Adherent adherent);

    Optional<Adherent> trouver(UUID id);
}
