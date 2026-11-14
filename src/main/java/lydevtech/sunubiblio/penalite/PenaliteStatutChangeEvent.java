package lydevtech.sunubiblio.penalite;

public record PenaliteStatutChangeEvent(
    String adherentId,
    boolean aDesImpayes,
    long montantTotalImpaye
) {}

