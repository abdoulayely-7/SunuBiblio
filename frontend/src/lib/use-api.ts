"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError } from "./api";

interface Etat<T> {
  source: () => Promise<T>;
  donnees: T | null;
  erreur: ApiError | null;
}

function enApiError(erreur: unknown): ApiError {
  return erreur instanceof ApiError
    ? erreur
    : new ApiError(0, "Erreur inattendue", "Une erreur inattendue s'est produite. Rechargez la page et réessayez.");
}

/**
 * Charge des donnees et expose l'attente, l'erreur (avec son message explicite) et un rechargement.
 * `chargeur` doit etre stable (useCallback) : quand il change, on recharge.
 */
export function useApi<T>(chargeur: () => Promise<T>) {
  const [etat, setEtat] = useState<Etat<T> | null>(null);
  const [version, setVersion] = useState(0);

  useEffect(() => {
    let annule = false;
    chargeur().then(
      (donnees) => {
        if (!annule) setEtat({ source: chargeur, donnees, erreur: null });
      },
      (erreur: unknown) => {
        if (!annule) setEtat({ source: chargeur, donnees: null, erreur: enApiError(erreur) });
      },
    );
    return () => {
      annule = true;
    };
  }, [chargeur, version]);

  const recharger = useCallback(() => setVersion((v) => v + 1), []);
  const aJour = etat !== null && etat.source === chargeur;

  return {
    donnees: etat?.donnees ?? null,
    erreur: aJour ? etat.erreur : null,
    /** Premiere attente, ou changement de parametres : on n'a pas encore de reponse pour la demande courante. */
    chargement: !aJour,
    recharger,
  };
}
