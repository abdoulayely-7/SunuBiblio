package lydevtech.sunubiblio.adherent;

public interface AdherentApi {

    AdherentDto inscrire(String nom, TypeAdherent type);

    /** Refuse, avec une explication, un adhérent inconnu. */
    AdherentDto trouver(String adherentId);

    /**
     * Refuse, avec une explication, un adhérent inconnu, bloqué par une pénalité impayée,
     * ou qui a déjà atteint son quota ({@code empruntsEnCours} est le nombre d'emprunts non rendus).
     */
    void verifierEligibiliteEmprunt(String adherentId, long empruntsEnCours);
}
