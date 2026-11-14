package lydevtech.sunubiblio.catalogue;

public record ExemplaireDto(
    String id,
    String livreIsbn,
    StatutExemplaire statut
) {}

