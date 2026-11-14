package lydevtech.sunubiblio.penalite;

public class PenaliteDejaRegleeException extends RuntimeException {

    public PenaliteDejaRegleeException(String penaliteId) {
        super("La pénalité " + penaliteId + " est déjà réglée : elle ne peut pas être payée deux fois.");
    }
}
