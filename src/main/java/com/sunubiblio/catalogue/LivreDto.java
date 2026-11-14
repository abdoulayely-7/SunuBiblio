package com.sunubiblio.catalogue;

import java.util.List;
import java.util.UUID;

public record LivreDto(UUID id, String titre, String auteur, int exemplairesDisponibles,
                       List<ExemplaireDto> exemplaires) {
}
