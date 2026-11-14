package lydevtech.sunubiblio.emprunts.internal.domain;

import java.util.UUID;

/**
 * Memoire locale du module emprunts : les penalites impayees qui bloquent un adherent.
 * Elle est alimentee par les evenements du module penalites, jamais par un appel vers lui.
 */
public interface BlocageRepository {

    void bloquer(UUID penaliteId, UUID adherentId);

    void debloquer(UUID penaliteId);

    boolean adherentBloque(UUID adherentId);
}
