package lydevtech.sunubiblio.catalogue;

import java.util.List;

public record LivreDto(
    String isbn,
    String titre,
    String auteur,
    List<ExemplaireDto> exemplaires
) {}

