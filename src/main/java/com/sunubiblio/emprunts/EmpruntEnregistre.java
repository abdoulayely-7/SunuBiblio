package com.sunubiblio.emprunts;

import java.time.LocalDate;
import java.util.UUID;

/** Evenement annonce par le module emprunts quand un exemplaire est emprunte. */
public record EmpruntEnregistre(UUID empruntId, UUID adherentId, UUID exemplaireId, LocalDate dateEcheance) {
}
