package lydevtech.sunubiblio.catalogue;

import java.util.UUID;

public record ExemplaireDto(UUID id, UUID livreId, EtatExemplaire etat) {
}
