package lydevtech.sunubiblio.emprunts.internal.infrastructure.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record EmpruntRequest(
        @NotNull(message = "L'identifiant de l'adhérent (adherentId) est obligatoire.")
        UUID adherentId,

        @NotNull(message = "L'identifiant de l'exemplaire (exemplaireId) est obligatoire.")
        UUID exemplaireId) {
}
