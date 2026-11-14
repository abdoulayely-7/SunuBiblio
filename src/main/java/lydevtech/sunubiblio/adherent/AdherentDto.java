package lydevtech.sunubiblio.adherent;

public record AdherentDto(
        String id,
        String nom,
        TypeAdherent type,
        int quotaEmprunts,
        boolean estBloquePourImpayes,
        long montantImpaye
) {}
