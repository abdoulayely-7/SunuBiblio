package lydevtech.sunubiblio.penalites;

import java.util.List;
import java.util.UUID;

public interface PenalitesApi {

    PenaliteDto creerPourRetard(UUID adherentId, UUID empruntId, int joursDeRetard);

    List<PenaliteDto> penalitesDe(UUID adherentId);

    PenaliteDto payer(UUID penaliteId);
}
