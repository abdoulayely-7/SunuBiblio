package lydevtech.sunubiblio.emprunts;

import java.time.LocalDate;
import java.util.UUID;

public record EmpruntDto(UUID id, UUID adherentId, UUID exemplaireId, LocalDate dateEmprunt,
                         LocalDate dateEcheance, LocalDate dateRetour) {
}
