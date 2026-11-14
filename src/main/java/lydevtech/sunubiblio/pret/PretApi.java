package lydevtech.sunubiblio.pret;

import java.time.LocalDate;
import java.util.List;

public interface PretApi {

    EmpruntDto emprunter(String adherentId, String exemplaireId);

    /** Restitue l'exemplaire ; sans date de retour (nulle), c'est la date du jour. */
    RestitutionDto restituer(String empruntId, LocalDate dateRetour);

    List<EmpruntDto> getEmpruntsEnCours(String adherentId);

    EmpruntDto trouver(String empruntId);
}
