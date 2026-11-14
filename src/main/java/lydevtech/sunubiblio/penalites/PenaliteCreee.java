package lydevtech.sunubiblio.penalites;

import java.util.UUID;

/** Evenement annonce par le module penalites quand une penalite de retard est creee. */
public record PenaliteCreee(UUID penaliteId, UUID adherentId, int montantXof) {
}
