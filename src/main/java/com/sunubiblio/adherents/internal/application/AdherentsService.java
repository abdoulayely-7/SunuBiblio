package com.sunubiblio.adherents.internal.application;

import com.sunubiblio.adherents.AdherentDto;
import com.sunubiblio.adherents.AdherentIntrouvableException;
import com.sunubiblio.adherents.AdherentsApi;
import com.sunubiblio.adherents.TypeAdherent;
import com.sunubiblio.adherents.internal.domain.Adherent;
import com.sunubiblio.adherents.internal.domain.AdherentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
class AdherentsService implements AdherentsApi {

    private final AdherentRepository adherents;

    AdherentsService(AdherentRepository adherents) {
        this.adherents = adherents;
    }

    @Override
    public AdherentDto inscrire(String nom, TypeAdherent type) {
        Adherent adherent = Adherent.nouveau(nom, type);
        adherents.ajouter(adherent);
        return versDto(adherent);
    }

    @Override
    @Transactional(readOnly = true)
    public AdherentDto trouver(UUID adherentId) {
        return versDto(adherents.trouver(adherentId).orElseThrow(() -> new AdherentIntrouvableException(adherentId)));
    }

    private static AdherentDto versDto(Adherent adherent) {
        return new AdherentDto(adherent.id(), adherent.nom(), adherent.type(), adherent.type().quotaEmprunts());
    }
}
