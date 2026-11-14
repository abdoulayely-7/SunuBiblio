package lydevtech.sunubiblio.penalite;

import java.util.List;

public interface PenaliteApi {

    PenaliteDto creerPenalite(String adherentId, String exemplaireId, long joursDeRetard);

    void reglerPenalite(String penaliteId);

    boolean aDesImpayes(String adherentId);

    List<PenaliteDto> getPenalitesAdherent(String adherentId);
}

