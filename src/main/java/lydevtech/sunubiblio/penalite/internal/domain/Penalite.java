package lydevtech.sunubiblio.penalite.internal.domain;

import lydevtech.sunubiblio.penalite.StatutPenalite;

import java.time.LocalDateTime;

public class Penalite {

    public static final long TARIF_PAR_JOUR_XOF = 100L;

    private final String id;
    private final String adherentId;
    private final String exemplaireId;
    private final long joursDeRetard;
    private final long montant;
    private final LocalDateTime dateCreation;
    private StatutPenalite statut;

    public Penalite(String id, String adherentId, String exemplaireId, long joursDeRetard) {
        if (joursDeRetard <= 0) {
            throw new IllegalArgumentException("Le nombre de jours de retard doit être strictement positif.");
        }
        if (adherentId == null || adherentId.isBlank()) {
            throw new IllegalArgumentException("L'identifiant adhérent est obligatoire.");
        }
        this.id = id;
        this.adherentId = adherentId;
        this.exemplaireId = exemplaireId;
        this.joursDeRetard = joursDeRetard;
        this.montant = joursDeRetard * TARIF_PAR_JOUR_XOF;
        this.dateCreation = LocalDateTime.now();
        this.statut = StatutPenalite.IMPAYEE;
    }

    public String getId() {
        return id;
    }

    public String getAdherentId() {
        return adherentId;
    }

    public String getExemplaireId() {
        return exemplaireId;
    }

    public long getJoursDeRetard() {
        return joursDeRetard;
    }

    public long getMontant() {
        return montant;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public StatutPenalite getStatut() {
        return statut;
    }

    public boolean isImpayee() {
        return this.statut == StatutPenalite.IMPAYEE;
    }

    public void regler() {
        if (this.statut == StatutPenalite.REGLEE) {
            throw new IllegalStateException("Cette pénalité a déjà été réglée.");
        }
        this.statut = StatutPenalite.REGLEE;
    }
}

