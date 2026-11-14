package com.sunubiblio.adherents;

import java.util.UUID;

public record AdherentDto(UUID id, String nom, TypeAdherent type, int quotaEmprunts) {
}
