package lydevtech.sunubiblio.emprunts;

import lydevtech.sunubiblio.penalites.PenaliteDto;

/** Resultat d'un retour : {@code penalite} est nulle quand l'exemplaire est rendu a temps. */
public record RetourDto(EmpruntDto emprunt, int joursDeRetard, PenaliteDto penalite) {
}
