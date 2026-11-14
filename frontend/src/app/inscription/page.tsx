"use client";

import { CalendarClock, Check, Copy, GraduationCap, LibraryBig, Presentation, UserPlus, Wallet } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { OuvrirCompteForm } from "@/components/ouvrir-compte-form";
import { useAdherent, useToast } from "@/components/providers";
import { Bouton, Carte, Champ, SurTitre, classesBouton, classesChamp } from "@/components/ui";
import { ApiError, inscrire } from "@/lib/api";
import type { Adherent, TypeAdherent } from "@/lib/types";
import { cn } from "@/lib/utils";

const TYPES: { valeur: TypeAdherent; libelle: string; detail: string; Icone: typeof GraduationCap }[] = [
  { valeur: "ETUDIANT", libelle: "Étudiant", detail: "3 livres à la fois", Icone: GraduationCap },
  { valeur: "ENSEIGNANT", libelle: "Enseignant", detail: "5 livres à la fois", Icone: Presentation },
];

const AVANTAGES = [
  { Icone: LibraryBig, titre: "Jusqu'à 5 livres à la fois", texte: "3 pour un étudiant, 5 pour un enseignant." },
  { Icone: CalendarClock, titre: "14 jours pour lire", texte: "Un retour à temps, et aucun frais." },
  { Icone: Wallet, titre: "Une règle simple", texte: "100 XOF par jour de retard, payables en ligne." },
];

function Bienvenue({ adherent }: { adherent: Adherent }) {
  const [copie, setCopie] = useState(false);

  async function copier() {
    try {
      await navigator.clipboard.writeText(adherent.id);
      setCopie(true);
      window.setTimeout(() => setCopie(false), 2500);
    } catch {
      // Presse-papiers refuse : l'identifiant reste visible et selectionnable.
    }
  }

  return (
    <div className="animate-pop flex flex-col gap-6">
      <div className="grid size-14 place-items-center rounded-full bg-forest-soft text-forest" aria-hidden>
        <Check className="size-7" />
      </div>
      <div>
        <h2 className="font-display text-3xl font-semibold text-ink">Bienvenue, {adherent.nom.split(" ")[0]}</h2>
        <p className="mt-2 leading-relaxed text-ink-muted">
          Votre compte est créé. Gardez précieusement votre identifiant : il vous permettra de retrouver votre compte.
        </p>
      </div>
      <div className="rounded-lg border border-line bg-paper p-4">
        <p className="text-xs font-semibold uppercase tracking-[0.16em] text-ink-faint">Votre identifiant d&apos;adhérent</p>
        <p className="mt-2 select-all break-all font-mono text-sm text-ink">{adherent.id}</p>
        <Bouton variante="secondaire" compact onClick={copier} className="mt-3">
          {copie ? <Check className="size-4 text-forest" aria-hidden /> : <Copy className="size-4" aria-hidden />}
          {copie ? "Copié" : "Copier l'identifiant"}
        </Bouton>
      </div>
      <div className="flex flex-col gap-3 sm:flex-row">
        <Link href="/" className={classesBouton("primaire")}>
          Parcourir le catalogue
        </Link>
        <Link href="/mon-compte" className={classesBouton("secondaire")}>
          Voir mon compte
        </Link>
      </div>
    </div>
  );
}

function FormulaireInscription({ apresInscription }: { apresInscription: (adherent: Adherent) => void }) {
  const { choisir } = useAdherent();
  const toast = useToast();
  const [nom, setNom] = useState("");
  const [type, setType] = useState<TypeAdherent>("ETUDIANT");
  const [touche, setTouche] = useState(false);
  const [erreurServeur, setErreurServeur] = useState<ApiError | null>(null);
  const [envoi, setEnvoi] = useState(false);

  const nomPropre = nom.trim();
  const erreurNom =
    erreurServeur?.pourChamp("nom") ??
    (touche && !nomPropre
      ? "Le nom de l'adhérent est obligatoire."
      : nomPropre.length > 255
        ? "Le nom ne doit pas dépasser 255 caractères."
        : undefined);

  async function soumettre(evenement: FormEvent) {
    evenement.preventDefault();
    setTouche(true);
    if (!nomPropre || nomPropre.length > 255) return;
    setEnvoi(true);
    setErreurServeur(null);
    try {
      const adherent = await inscrire(nomPropre, type);
      choisir(adherent);
      toast.succes(`${adherent.nom} est inscrit comme ${adherent.type === "ENSEIGNANT" ? "enseignant" : "étudiant"}.`, "Inscription réussie");
      apresInscription(adherent);
    } catch (cause) {
      if (cause instanceof ApiError && cause.erreurs.length > 0) setErreurServeur(cause);
      else toast.erreur(cause);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <form onSubmit={soumettre} noValidate className="flex flex-col gap-6">
      <Champ label="Nom complet" erreur={erreurNom} aide="Tel qu'il apparaîtra sur votre compte.">
        {(props) => (
          <input
            {...props}
            value={nom}
            onChange={(e) => {
              setNom(e.target.value);
              setErreurServeur(null);
            }}
            onBlur={() => setTouche(true)}
            placeholder="Awa Diop"
            autoComplete="name"
            maxLength={300}
            className={classesChamp}
          />
        )}
      </Champ>

      <fieldset>
        <legend className="mb-2 text-sm font-medium text-ink">Vous êtes</legend>
        <div className="grid gap-3 sm:grid-cols-2">
          {TYPES.map(({ valeur, libelle, detail, Icone }) => (
            <label
              key={valeur}
              className={cn(
                "relative flex min-h-16 cursor-pointer items-center gap-3 rounded-lg border bg-surface p-4 transition-all duration-200",
                "has-[:focus-visible]:outline-2 has-[:focus-visible]:outline-offset-2 has-[:focus-visible]:outline-focus",
                type === valeur ? "border-forest bg-forest-soft shadow-card" : "border-line-strong hover:border-ink-faint",
              )}
            >
              <input
                type="radio"
                name="type"
                value={valeur}
                checked={type === valeur}
                onChange={() => setType(valeur)}
                className="sr-only"
              />
              <span
                className={cn(
                  "grid size-10 place-items-center rounded-full transition-colors",
                  type === valeur ? "bg-forest text-surface" : "bg-paper-deep text-ink-muted",
                )}
                aria-hidden
              >
                <Icone className="size-5" />
              </span>
              <span>
                <span className="block font-medium text-ink">{libelle}</span>
                <span className="block text-sm text-ink-muted">{detail}</span>
              </span>
              {type === valeur && <Check className="absolute right-3 top-3 size-4 text-forest" aria-hidden />}
            </label>
          ))}
        </div>
        {erreurServeur?.pourChamp("type") && (
          <p role="alert" className="mt-2 text-sm text-terracotta-deep">
            {erreurServeur.pourChamp("type")}
          </p>
        )}
      </fieldset>

      <Bouton type="submit" chargement={envoi} className="w-full sm:w-auto">
        <UserPlus className="size-4" aria-hidden />
        Créer mon compte adhérent
      </Bouton>
    </form>
  );
}

export default function PageInscription() {
  const router = useRouter();
  const [mode, setMode] = useState<"creer" | "ouvrir">("creer");
  const [nouveau, setNouveau] = useState<Adherent | null>(null);

  return (
    <div className="grid gap-12 lg:grid-cols-[1fr_minmax(0,34rem)] lg:gap-20">
      <section className="animate-rise">
        <SurTitre>Adhésion</SurTitre>
        <h1 className="mt-3 text-balance font-display text-[2.5rem] font-semibold leading-[1.08] text-ink sm:text-5xl">
          Rejoignez la bibliothèque, <span className="italic text-forest">empruntez dès aujourd&apos;hui.</span>
        </h1>
        <p className="mt-5 max-w-xl text-lg leading-relaxed text-ink-muted">
          Une minute pour créer votre compte, aucun mot de passe à retenir : votre identifiant d&apos;adhérent suffit.
        </p>
        <ul className="mt-10 flex flex-col gap-6">
          {AVANTAGES.map(({ Icone, titre, texte }) => (
            <li key={titre} className="flex items-start gap-4">
              <span className="grid size-11 shrink-0 place-items-center rounded-lg bg-forest-soft text-forest" aria-hidden>
                <Icone className="size-5" />
              </span>
              <div>
                <p className="font-display text-lg font-semibold text-ink">{titre}</p>
                <p className="mt-0.5 text-ink-muted">{texte}</p>
              </div>
            </li>
          ))}
        </ul>
      </section>

      <Carte className="animate-rise p-6 sm:p-8" >
        {nouveau ? (
          <Bienvenue adherent={nouveau} />
        ) : (
          <>
            <div role="tablist" aria-label="Mode d'accès" className="mb-7 grid grid-cols-2 rounded-lg bg-paper-deep p-1">
              {(
                [
                  ["creer", "Créer un compte"],
                  ["ouvrir", "J'ai un identifiant"],
                ] as const
              ).map(([valeur, libelle]) => (
                <button
                  key={valeur}
                  type="button"
                  role="tab"
                  aria-selected={mode === valeur}
                  onClick={() => setMode(valeur)}
                  className={cn(
                    "min-h-11 rounded-md px-3 text-sm font-medium transition-all duration-200",
                    mode === valeur ? "bg-surface text-ink shadow-card" : "text-ink-muted hover:text-ink",
                  )}
                >
                  {libelle}
                </button>
              ))}
            </div>
            {mode === "creer" ? (
              <FormulaireInscription apresInscription={setNouveau} />
            ) : (
              <OuvrirCompteForm apresOuverture={() => router.push("/mon-compte")} />
            )}
          </>
        )}
      </Carte>
    </div>
  );
}
