package lydevtech.sunubiblio.adherents.internal.infrastructure.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lydevtech.sunubiblio.adherents.TypeAdherent;

record InscriptionRequest(
        @NotBlank(message = "Le nom de l'adhérent est obligatoire.")
        @Size(max = 255, message = "Le nom de l'adhérent ne doit pas dépasser 255 caractères.")
        String nom,

        @NotNull(message = "Le type d'adhérent est obligatoire : ETUDIANT ou ENSEIGNANT.")
        TypeAdherent type) {
}
