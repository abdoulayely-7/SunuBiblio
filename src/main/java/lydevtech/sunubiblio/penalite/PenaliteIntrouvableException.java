package lydevtech.sunubiblio.penalite;

public class PenaliteIntrouvableException extends RuntimeException {

    public PenaliteIntrouvableException(String penaliteId) {
        super("Aucune pénalité ne correspond à l'identifiant " + penaliteId + " : vérifiez-le dans la liste des pénalités de l'adhérent.");
    }
}
