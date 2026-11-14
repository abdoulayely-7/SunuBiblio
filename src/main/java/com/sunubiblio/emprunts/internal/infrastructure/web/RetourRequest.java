package com.sunubiblio.emprunts.internal.infrastructure.web;

import java.time.LocalDate;

/** Corps optionnel du retour : sans date, c'est la date du jour (la date permet de rejouer un retard a la main). */
record RetourRequest(LocalDate dateRetour) {
}
