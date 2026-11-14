package com.sunubiblio.penalites;

import java.util.List;
import java.util.UUID;

public interface PenalitesApi {

    PenaliteDto creerPourRetard(UUID adherentId, UUID empruntId, int joursDeRetard);

    boolean aPenaliteImpayee(UUID adherentId);

    List<PenaliteDto> penalitesDe(UUID adherentId);

    PenaliteDto payer(UUID penaliteId);
}
