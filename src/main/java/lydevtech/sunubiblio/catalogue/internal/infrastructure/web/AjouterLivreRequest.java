package lydevtech.sunubiblio.catalogue.internal.infrastructure.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record AjouterLivreRequest(
        @NotBlank(message = "Le titre du livre est obligatoire.")
        @Size(max = 255, message = "Le titre du livre ne doit pas dépasser 255 caractères.")
        String titre,

        @NotBlank(message = "L'auteur du livre est obligatoire.")
        @Size(max = 255, message = "Le nom de l'auteur ne doit pas dépasser 255 caractères.")
        String auteur,

        @NotNull(message = "Le nombre d'exemplaires est obligatoire.")
        @Min(value = 1, message = "Un livre doit avoir au moins 1 exemplaire.")
        @Max(value = 500, message = "Un livre ne peut pas être créé avec plus de 500 exemplaires à la fois.")
        Integer nombreExemplaires) {
}
