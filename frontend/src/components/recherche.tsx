"use client";

import { Search, X } from "lucide-react";
import { useEffect, useRef, type ReactNode } from "react";
import { cn } from "@/lib/utils";

interface BarreRechercheProps {
  valeur: string;
  onChange: (valeur: string) => void;
  libelle: string;
  placeholder: string;
  /** Texte annonce aux lecteurs d'ecran a chaque changement de resultats. */
  annonce?: string;
  taille?: "normale" | "grande";
  className?: string;
}

/** Barre de recherche : bouton d'effacement, raccourci « / » pour la focaliser, Echap pour la vider. */
export function BarreRecherche({
  valeur,
  onChange,
  libelle,
  placeholder,
  annonce,
  taille = "normale",
  className,
}: BarreRechercheProps) {
  const champ = useRef<HTMLInputElement>(null);

  useEffect(() => {
    function auClavier(evenement: KeyboardEvent) {
      const cible = evenement.target as HTMLElement | null;
      const saisie = cible && (cible.tagName === "INPUT" || cible.tagName === "TEXTAREA" || cible.isContentEditable);
      if (evenement.key === "/" && !saisie && !evenement.metaKey && !evenement.ctrlKey) {
        evenement.preventDefault();
        champ.current?.focus();
      }
    }
    window.addEventListener("keydown", auClavier);
    return () => window.removeEventListener("keydown", auClavier);
  }, []);

  const grande = taille === "grande";

  return (
    <div className={cn("relative", className)} role="search">
      <label htmlFor="recherche" className="sr-only">
        {libelle}
      </label>
      <Search
        className={cn("pointer-events-none absolute top-1/2 -translate-y-1/2 text-ink-faint", grande ? "left-5 size-5" : "left-4 size-4")}
        aria-hidden
      />
      <input
        id="recherche"
        ref={champ}
        type="search"
        inputMode="search"
        autoComplete="off"
        value={valeur}
        onChange={(evenement) => onChange(evenement.target.value)}
        onKeyDown={(evenement) => {
          if (evenement.key === "Escape" && valeur) {
            evenement.preventDefault();
            onChange("");
          }
        }}
        placeholder={placeholder}
        className={cn(
          "w-full rounded-lg border border-line-strong bg-surface text-ink shadow-card transition-all",
          "placeholder:text-ink-faint hover:border-ink-faint focus:border-forest focus:outline-none focus:ring-4 focus:ring-forest/15",
          "[&::-webkit-search-cancel-button]:hidden",
          grande ? "h-14 pl-13 pr-24 text-lg" : "h-11 pl-11 pr-20 text-[0.9375rem]",
        )}
      />
      <div className={cn("absolute top-1/2 flex -translate-y-1/2 items-center gap-2", grande ? "right-3" : "right-2")}>
        {valeur ? (
          <button
            type="button"
            onClick={() => {
              onChange("");
              champ.current?.focus();
            }}
            aria-label="Effacer la recherche"
            className="grid size-9 place-items-center rounded-md text-ink-faint transition-colors hover:bg-paper-deep hover:text-ink"
          >
            <X className="size-4" aria-hidden />
          </button>
        ) : (
          <kbd
            aria-hidden
            className="hidden rounded-md border border-line-strong bg-paper px-2 py-1 font-mono text-xs text-ink-faint sm:inline"
          >
            /
          </kbd>
        )}
      </div>
      <p className="sr-only" aria-live="polite">
        {annonce}
      </p>
    </div>
  );
}

function echapper(texte: string) {
  return texte.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

/** Surligne dans `texte` chaque mot de la recherche (sans tenir compte des majuscules). */
export function Surbrillance({ texte, requete }: { texte: string; requete: string }): ReactNode {
  const mots = requete.trim().split(/\s+/).filter(Boolean);
  if (mots.length === 0) return texte;
  const motif = new RegExp(`(${mots.map(echapper).join("|")})`, "gi");
  return texte.split(motif).map((morceau, index) =>
    index % 2 === 1 ? (
      <mark key={index} className="rounded-sm bg-gold-soft px-0.5 text-inherit">
        {morceau}
      </mark>
    ) : (
      morceau
    ),
  );
}
