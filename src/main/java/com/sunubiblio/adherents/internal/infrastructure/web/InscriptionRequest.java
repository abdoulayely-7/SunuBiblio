package com.sunubiblio.adherents.internal.infrastructure.web;

import com.sunubiblio.adherents.TypeAdherent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record InscriptionRequest(@NotBlank String nom, @NotNull TypeAdherent type) {
}
