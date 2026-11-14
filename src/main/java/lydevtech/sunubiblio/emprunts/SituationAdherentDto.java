package lydevtech.sunubiblio.emprunts;

import lydevtech.sunubiblio.penalites.PenaliteDto;

import java.util.List;
import java.util.UUID;

public record SituationAdherentDto(UUID adherentId, List<EmpruntDto> empruntsEnCours, List<PenaliteDto> penalites) {
}
