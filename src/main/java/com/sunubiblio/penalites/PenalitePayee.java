package com.sunubiblio.penalites;

import java.util.UUID;

/** Evenement annonce par le module penalites quand une penalite est payee. */
public record PenalitePayee(UUID penaliteId, UUID adherentId, int montantXof) {
}
