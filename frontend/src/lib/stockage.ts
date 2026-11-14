"use client";

import { useSyncExternalStore } from "react";

const abonnes = new Set<() => void>();

function lire(cle: string): string | null {
  try {
    return window.localStorage.getItem(cle);
  } catch {
    return null;
  }
}

/** Ecrit (ou efface, avec `null`) une valeur locale et previent les composants qui la lisent. */
export function ecrire(cle: string, valeur: string | null) {
  try {
    if (valeur === null) {
      window.localStorage.removeItem(cle);
    } else {
      window.localStorage.setItem(cle, valeur);
    }
  } catch {
    // Stockage indisponible (navigation privee, quota) : l'application fonctionne, sans memoire.
  }
  abonnes.forEach((prevenir) => prevenir());
}

function sAbonner(prevenir: () => void) {
  abonnes.add(prevenir);
  window.addEventListener("storage", prevenir);
  return () => {
    abonnes.delete(prevenir);
    window.removeEventListener("storage", prevenir);
  };
}

/** Lit une valeur locale sans decalage d'hydratation : `null` cote serveur et au premier rendu. */
export function useStockage(cle: string): string | null {
  return useSyncExternalStore(
    sAbonner,
    () => lire(cle),
    () => null,
  );
}

const jamais = () => () => {};

/** Vrai des que le navigateur a pris la main (permet d'eviter d'afficher un etat « vide » trop tot). */
export function useHydrate(): boolean {
  return useSyncExternalStore(
    jamais,
    () => true,
    () => false,
  );
}
