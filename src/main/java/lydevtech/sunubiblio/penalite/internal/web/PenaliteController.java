package lydevtech.sunubiblio.penalite.internal.web;

import lydevtech.sunubiblio.penalite.PenaliteApi;
import lydevtech.sunubiblio.penalite.PenaliteDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/penalites")
public class PenaliteController {

    private final PenaliteApi penaliteApi;

    public PenaliteController(PenaliteApi penaliteApi) {
        this.penaliteApi = penaliteApi;
    }

    @PostMapping("/{id}/payer")
    public ResponseEntity<Void> payerPenalite(@PathVariable String id) {
        penaliteApi.reglerPenalite(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<PenaliteDto> getPenalitesAdherent(@RequestParam String adherentId) {
        return penaliteApi.getPenalitesAdherent(adherentId);
    }
}

