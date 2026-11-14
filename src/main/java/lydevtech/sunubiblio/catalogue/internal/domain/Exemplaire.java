package lydevtech.sunubiblio.catalogue.internal.domain;

import lydevtech.sunubiblio.catalogue.StatutExemplaire;

public class Exemplaire {

    private final String id;
    private final String livreIsbn;
    private StatutExemplaire statut;

    public Exemplaire(String id, String livreIsbn) {
        this.id = id;
        this.livreIsbn = livreIsbn;
        this.statut = StatutExemplaire.DISPONIBLE;
    }

    public String getId() {
        return id;
    }

    public String getLivreIsbn() {
        return livreIsbn;
    }

    public StatutExemplaire getStatut() {
        return statut;
    }

    public boolean isDisponible() {
        return this.statut == StatutExemplaire.DISPONIBLE;
    }

    public void emprunter() {
        if (!isDisponible()) {
            throw new IllegalStateException("L'exemplaire " + id + " n'est pas disponible pour l'emprunt.");
        }
        this.statut = StatutExemplaire.EMPRUNTE;
    }

    public void rendre() {
        if (this.statut == StatutExemplaire.RETIRE) {
            throw new IllegalStateException("Impossible de rendre un exemplaire retiré.");
        }
        this.statut = StatutExemplaire.DISPONIBLE;
    }

    public void retirer() {
        this.statut = StatutExemplaire.RETIRE;
    }
}

