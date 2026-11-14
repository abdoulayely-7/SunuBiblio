"use client";

import { useEffect, useState } from "react";

/** Valeur qui ne suit la saisie qu'apres un court temps de repos : evite un appel API a chaque touche. */
export function useDebounce<T>(valeur: T, delaiMs = 300): T {
  const [differee, setDifferee] = useState(valeur);

  useEffect(() => {
    const minuteur = window.setTimeout(() => setDifferee(valeur), delaiMs);
    return () => window.clearTimeout(minuteur);
  }, [valeur, delaiMs]);

  return differee;
}
