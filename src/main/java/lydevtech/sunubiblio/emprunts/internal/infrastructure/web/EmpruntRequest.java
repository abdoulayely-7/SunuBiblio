package lydevtech.sunubiblio.emprunts.internal.infrastructure.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

record EmpruntRequest(@NotNull UUID adherentId, @NotNull UUID exemplaireId) {
}
