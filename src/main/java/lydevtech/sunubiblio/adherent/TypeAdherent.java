package lydevtech.sunubiblio.adherent;

public enum TypeAdherent {
    ETUDIANT(3),
    ENSEIGNANT(5);

    private final int quotaEmprunts;

    TypeAdherent(int quotaEmprunts) {
        this.quotaEmprunts = quotaEmprunts;
    }

    /** Nombre maximal d'exemplaires qu'un adhérent de ce type peut emprunter à la fois. */
    public int quotaEmprunts() {
        return quotaEmprunts;
    }
}
