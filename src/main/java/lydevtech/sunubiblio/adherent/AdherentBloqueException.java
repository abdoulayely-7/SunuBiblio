package lydevtech.sunubiblio.adherent;

public class AdherentBloqueException extends RuntimeException {

    public AdherentBloqueException(String adherentId, long montantImpaye) {
        super("L'adhérent " + adherentId + " a " + montantImpaye + " XOF de pénalités impayées : "
                + "il doit d'abord les régler avant de pouvoir emprunter de nouveau.");
    }
}
