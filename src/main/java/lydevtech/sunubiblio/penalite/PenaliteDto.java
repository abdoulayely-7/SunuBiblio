package lydevtech.sunubiblio.penalite;

import java.time.LocalDateTime;

public record PenaliteDto(
    String id,
    String adherentId,
    String exemplaireId,
    long joursDeRetard,
    long montant,
    StatutPenalite statut,
    LocalDateTime dateCreation
) {}

