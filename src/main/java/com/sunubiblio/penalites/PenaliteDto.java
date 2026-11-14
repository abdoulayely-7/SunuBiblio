package com.sunubiblio.penalites;

import java.util.UUID;

public record PenaliteDto(UUID id, UUID adherentId, UUID empruntId, int joursDeRetard, int montantXof,
                          StatutPenalite statut) {
}
