package lydevtech.sunubiblio.catalogue.internal.domain;

import lydevtech.sunubiblio.catalogue.ExemplaireEtatInvalideException;
import lydevtech.sunubiblio.catalogue.ExemplaireIndisponibleException;
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

    /** Reconstruit un exemplaire déjà enregistré, dans son état courant. */
    public static Exemplaire restaurer(String id, String livreIsbn, StatutExemplaire statut) {
        Exemplaire exemplaire = new Exemplaire(id, livreIsbn);
        exemplaire.statut = statut;
        return exemplaire;
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
            throw new ExemplaireIndisponibleException(id, statut);
        }
        this.statut = StatutExemplaire.EMPRUNTE;
    }

    public void rendre() {
        if (this.statut != StatutExemplaire.EMPRUNTE) {
            throw new ExemplaireEtatInvalideException(id, "il n'est pas en cours d'emprunt.");
        }
        this.statut = StatutExemplaire.DISPONIBLE;
    }

    public void retirer() {
        if (this.statut == StatutExemplaire.EMPRUNTE) {
            throw new ExemplaireEtatInvalideException(id, "il est en cours d'emprunt : il doit d'abord être rendu.");
        }
        this.statut = StatutExemplaire.RETIRE;
    }
}
