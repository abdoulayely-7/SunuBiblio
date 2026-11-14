package lydevtech.sunubiblio.adherent.internal.domain;

import lydevtech.sunubiblio.adherent.AdherentBloqueException;
import lydevtech.sunubiblio.adherent.QuotaEmpruntsAtteintException;
import lydevtech.sunubiblio.adherent.TypeAdherent;

public class Adherent {

    private final String id;
    private final String nom;
    private final TypeAdherent type;
    private boolean estBloquePourImpayes;
    private long montantImpaye;

    public Adherent(String id, String nom, TypeAdherent type) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'adhérent est obligatoire.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Le type de l'adhérent est obligatoire (ETUDIANT ou ENSEIGNANT).");
        }
        this.id = id;
        this.nom = nom;
        this.type = type;
    }

    /** Reconstruit un adhérent déjà enregistré, dans son état courant. */
    public static Adherent restaurer(String id, String nom, TypeAdherent type, boolean estBloquePourImpayes,
                                     long montantImpaye) {
        Adherent adherent = new Adherent(id, nom, type);
        adherent.estBloquePourImpayes = estBloquePourImpayes;
        adherent.montantImpaye = montantImpaye;
        return adherent;
    }

    public String getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public TypeAdherent getType() {
        return type;
    }

    public boolean isEstBloquePourImpayes() {
        return estBloquePourImpayes;
    }

    public long getMontantImpaye() {
        return montantImpaye;
    }

    /** Reflète l'état des pénalités tel qu'annoncé par le module penalite. */
    public void mettreAJourImpayes(boolean aDesImpayes, long montantTotalImpaye) {
        this.estBloquePourImpayes = aDesImpayes;
        this.montantImpaye = aDesImpayes ? montantTotalImpaye : 0;
    }

    /** Le blocage prime sur le quota : un adhérent bloqué doit d'abord régler ses pénalités. */
    public void verifierEligibiliteEmprunt(long empruntsEnCours) {
        if (estBloquePourImpayes) {
            throw new AdherentBloqueException(id, montantImpaye);
        }
        if (empruntsEnCours >= type.quotaEmprunts()) {
            throw new QuotaEmpruntsAtteintException(id, type.quotaEmprunts());
        }
    }
}
