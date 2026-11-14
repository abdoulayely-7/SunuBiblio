package com.sunubiblio.catalogue.internal.infrastructure.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

record AjouterLivreRequest(@NotBlank String titre, @NotBlank String auteur, @Min(1) int nombreExemplaires) {
}
