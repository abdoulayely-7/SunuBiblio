package com.sunubiblio.adherents;

public enum TypeAdherent {
    ETUDIANT(3),
    ENSEIGNANT(5);

    private final int quotaEmprunts;

    TypeAdherent(int quotaEmprunts) {
        this.quotaEmprunts = quotaEmprunts;
    }

    /** Nombre maximal d'exemplaires qu'un adherent de ce type peut emprunter a la fois. */
    public int quotaEmprunts() {
        return quotaEmprunts;
    }
}
