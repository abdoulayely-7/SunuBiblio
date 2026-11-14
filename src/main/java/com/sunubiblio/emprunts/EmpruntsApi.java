package com.sunubiblio.emprunts;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EmpruntsApi {

    EmpruntDto emprunter(UUID adherentId, UUID exemplaireId);

    /** Rend l'exemplaire ; sans date de retour (nulle), c'est la date du jour. */
    RetourDto rendre(UUID empruntId, LocalDate dateRetour);

    EmpruntDto trouver(UUID empruntId);

    List<EmpruntDto> empruntsEnCours(UUID adherentId);

    SituationAdherentDto situation(UUID adherentId);
}
