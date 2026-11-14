package lydevtech.sunubiblio.pret;

import java.time.LocalDate;

public record EmpruntDto(
        String id,
        String adherentId,
        String exemplaireId,
        LocalDate dateEmprunt,
        LocalDate dateEcheance,
        LocalDate dateRetour,
        StatutEmprunt statut
) {}
