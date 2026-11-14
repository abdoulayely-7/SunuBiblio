"use client";

import { KeyRound } from "lucide-react";
import { useState, type FormEvent } from "react";
import { ApiError, trouverAdherent } from "@/lib/api";
import { ID_ADHERENT_VALIDE } from "@/lib/format";
import { useAdherent, useToast } from "./providers";
import { Bouton, Champ, classesChamp } from "./ui";

/** Retrouver un compte existant avec son identifiant (il n'y a pas de mot de passe dans ce TP). */
export function OuvrirCompteForm({ apresOuverture }: { apresOuverture?: () => void }) {
  const { choisir } = useAdherent();
  const toast = useToast();
  const [identifiant, setIdentifiant] = useState("");
  const [touche, setTouche] = useState(false);
  const [erreurServeur, setErreurServeur] = useState<string>();
  const [envoi, setEnvoi] = useState(false);

  const saisie = identifiant.trim();
  const erreurLocale = !saisie
    ? "Saisissez l'identifiant de l'adhérent."
    : !ID_ADHERENT_VALIDE.test(saisie)
      ? "Un identifiant d'adhérent ressemble à ADH-3F9A1C2E."
      : undefined;
  const erreur = erreurServeur ?? (touche ? erreurLocale : undefined);

  async function soumettre(evenement: FormEvent) {
    evenement.preventDefault();
    setTouche(true);
    if (erreurLocale) return;
    setEnvoi(true);
    setErreurServeur(undefined);
    try {
      const adherent = await trouverAdherent(saisie.toUpperCase());
      choisir(adherent);
      toast.succes(`Compte de ${adherent.nom} ouvert.`, "Bon retour");
      apresOuverture?.();
    } catch (cause) {
      if (cause instanceof ApiError && !cause.estPanneServeur) setErreurServeur(cause.detail);
      else toast.erreur(cause);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <form onSubmit={soumettre} noValidate className="flex flex-col gap-4">
      <Champ label="Identifiant de l'adhérent" erreur={erreur} aide="Celui qui vous a été donné à l'inscription.">
        {(props) => (
          <input
            {...props}
            value={identifiant}
            onChange={(e) => {
              setIdentifiant(e.target.value);
              setErreurServeur(undefined);
            }}
            onBlur={() => setTouche(true)}
            placeholder="ADH-3F9A1C2E"
            autoComplete="off"
            spellCheck={false}
            className={`${classesChamp} font-mono text-sm`}
          />
        )}
      </Champ>
      <Bouton type="submit" variante="secondaire" chargement={envoi}>
        <KeyRound className="size-4" aria-hidden />
        Ouvrir mon compte
      </Bouton>
    </form>
  );
}
