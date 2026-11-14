package lydevtech.sunubiblio.pret;

import lydevtech.sunubiblio.penalite.PenaliteDto;

/** Résultat d'une restitution : {@code penalite} est nulle quand l'exemplaire est rendu à temps. */
public record RestitutionDto(
        EmpruntDto emprunt,
        int joursDeRetard,
        PenaliteDto penalite
) {}
