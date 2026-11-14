package lydevtech.sunubiblio.catalogue.internal.domain;

import lydevtech.sunubiblio.catalogue.EtatExemplaire;
import lydevtech.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import lydevtech.sunubiblio.catalogue.ExemplaireIndisponibleException;

import java.util.UUID;

public record Exemplaire(UUID id, UUID livreId, EtatExemplaire etat) {

    public static Exemplaire neuf(UUID livreId) {
        return new Exemplaire(UUID.randomUUID(), livreId, EtatExemplaire.DISPONIBLE);
    }

    public Exemplaire emprunte() {
        if (etat != EtatExemplaire.DISPONIBLE) {
            throw new ExemplaireIndisponibleException(id);
        }
        return avecEtat(EtatExemplaire.EMPRUNTE);
    }

    public Exemplaire rendu() {
        if (etat != EtatExemplaire.EMPRUNTE) {
            throw new ExemplaireEtatInvalideException(id, "il n'est pas en cours d'emprunt");
        }
        return avecEtat(EtatExemplaire.DISPONIBLE);
    }

    public Exemplaire retire() {
        if (etat == EtatExemplaire.EMPRUNTE) {
            throw new ExemplaireEtatInvalideException(id, "il est en cours d'emprunt, il doit d'abord être rendu");
        }
        return avecEtat(EtatExemplaire.RETIRE);
    }

    private Exemplaire avecEtat(EtatExemplaire nouvelEtat) {
        return new Exemplaire(id, livreId, nouvelEtat);
    }
}
