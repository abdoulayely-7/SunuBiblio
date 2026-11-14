"use client";

import { ArrowRight, BookCheck, BookMarked, LibraryBig, SearchX } from "lucide-react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useCallback, useEffect, useState } from "react";
import { LivreCover } from "@/components/livre-cover";
import { useAdherent, useToast } from "@/components/providers";
import { BarreRecherche, Surbrillance } from "@/components/recherche";
import { Alerte, Badge, Bouton, EtatVide, Squelette, SurTitre, classesBouton } from "@/components/ui";
import { emprunter, listerLivres } from "@/lib/api";
import { formaterDate, pluriel } from "@/lib/format";
import { useApi } from "@/lib/use-api";
import { useDebounce } from "@/lib/use-debounce";
import { exemplairesDisponibles, type Livre } from "@/lib/types";
import { cn } from "@/lib/utils";

function CarteLivre({
  livre,
  requete,
  enCours,
  onEmprunter,
  aUnAdherent,
  index,
}: {
  livre: Livre;
  requete: string;
  enCours: boolean;
  onEmprunter: () => void;
  aUnAdherent: boolean;
  index: number;
}) {
  const disponible = exemplairesDisponibles(livre) > 0;

  return (
    <li
      className="animate-rise group flex flex-col rounded-lg border border-line bg-surface p-4 shadow-card transition-all duration-300 ease-soft hover:-translate-y-1 hover:shadow-lift"
      style={{ animationDelay: `${Math.min(index, 11) * 45}ms` }}
    >
      <LivreCover titre={livre.titre} auteur={livre.auteur} className="transition-transform duration-500 ease-soft group-hover:scale-[1.02]" />
      <div className="mt-4 flex flex-1 flex-col">
        <h3 className="font-display text-lg font-semibold leading-snug text-ink">
          <Surbrillance texte={livre.titre} requete={requete} />
        </h3>
        <p className="mt-1 text-sm text-ink-muted">
          <Surbrillance texte={livre.auteur} requete={requete} />
        </p>
        <p className="mt-0.5 font-mono text-[0.7rem] text-ink-faint">
          ISBN <Surbrillance texte={livre.isbn} requete={requete} />
        </p>
        <div className="mt-3 flex items-center justify-between gap-2">
          {disponible ? (
            <Badge ton="forest">
              <span className="size-1.5 rounded-full bg-forest" aria-hidden />
              {pluriel(exemplairesDisponibles(livre), "disponible")}
            </Badge>
          ) : (
            <Badge ton="terracotta">
              <span className="size-1.5 rounded-full bg-terracotta" aria-hidden />
              Tous empruntés
            </Badge>
          )}
          <span className="text-xs text-ink-faint">{pluriel(livre.exemplaires.length, "exemplaire")}</span>
        </div>

        <div className="mt-4 flex-1" />
        {aUnAdherent ? (
          <Bouton onClick={onEmprunter} disabled={!disponible} chargement={enCours} className="w-full">
            <BookMarked className="size-4" aria-hidden />
            {disponible ? "Emprunter" : "Indisponible"}
          </Bouton>
        ) : (
          <Link href="/inscription" className={cn(classesBouton("secondaire"), "w-full")}>
            Se connecter pour emprunter
          </Link>
        )}
      </div>
    </li>
  );
}

function Catalogue() {
  const router = useRouter();
  const parametres = useSearchParams();
  const toast = useToast();
  const { pret, adherent } = useAdherent();

  const [saisie, setSaisie] = useState(parametres.get("q") ?? "");
  const [disponiblesSeulement, setDisponiblesSeulement] = useState(false);
  const [enCours, setEnCours] = useState<string | null>(null);
  const requete = useDebounce(saisie.trim(), 300);

  // Le terme cherche reste dans l'adresse : on peut partager ou retrouver une recherche.
  useEffect(() => {
    const cible = requete ? `/?q=${encodeURIComponent(requete)}` : "/";
    router.replace(cible, { scroll: false });
  }, [requete, router]);

  const chargeur = useCallback(() => listerLivres(requete), [requete]);
  const { donnees, erreur, chargement, recharger } = useApi(chargeur);

  const livres = donnees ?? [];
  const affiches = disponiblesSeulement ? livres.filter((l) => exemplairesDisponibles(l) > 0) : livres;
  const totalDisponibles = livres.reduce((somme, l) => somme + exemplairesDisponibles(l), 0);
  const enAttente = chargement && donnees === null;

  async function lancerEmprunt(livre: Livre) {
    if (!adherent) return;
    const exemplaire = livre.exemplaires.find((e) => e.statut === "DISPONIBLE");
    if (!exemplaire) {
      toast.info(
        "Tous les exemplaires de ce livre viennent d'être empruntés. La liste est actualisée : réessayez quand l'un d'eux sera rendu.",
        "Plus d'exemplaire disponible",
      );
      recharger();
      return;
    }
    setEnCours(livre.isbn);
    try {
      const emprunt = await emprunter(adherent.id, exemplaire.id);
      toast.succes(`« ${livre.titre} » est à vous jusqu'au ${formaterDate(emprunt.dateEcheance)}.`, "Emprunt enregistré");
      recharger();
    } catch (cause) {
      toast.erreur(cause);
      recharger();
    } finally {
      setEnCours(null);
    }
  }

  const annonce = enAttente
    ? "Recherche en cours"
    : `${pluriel(affiches.length, "livre")} ${affiches.length > 1 ? "trouvés" : "trouvé"}`;

  return (
    <div className="flex flex-col gap-10">
      <section className="animate-rise">
        <SurTitre>Bibliothèque universitaire</SurTitre>
        <h1 className="mt-3 max-w-3xl text-balance font-display text-[2.5rem] font-semibold leading-[1.08] text-ink sm:text-6xl">
          Un livre vous attend, <span className="italic text-forest">il suffit de le chercher.</span>
        </h1>
        <p className="mt-5 max-w-2xl text-lg leading-relaxed text-ink-muted">
          Parcourez le catalogue, voyez en un coup d&apos;œil les exemplaires disponibles et empruntez pour 14 jours.
        </p>

        <div className="mt-8 max-w-3xl">
          <BarreRecherche
            taille="grande"
            valeur={saisie}
            onChange={setSaisie}
            libelle="Rechercher un livre par titre, par auteur ou par ISBN"
            placeholder="Titre, auteur ou ISBN… essayez « Mariama Bâ »"
            annonce={annonce}
          />
        </div>

        <div className="mt-5 flex flex-wrap items-center gap-x-6 gap-y-3">
          <button
            type="button"
            role="switch"
            aria-checked={disponiblesSeulement}
            onClick={() => setDisponiblesSeulement((v) => !v)}
            className="group flex min-h-11 items-center gap-3 text-sm font-medium text-ink"
          >
            <span
              className={cn(
                "relative h-6 w-11 rounded-full transition-colors duration-200",
                disponiblesSeulement ? "bg-forest" : "bg-line-strong",
              )}
              aria-hidden
            >
              <span
                className={cn(
                  "absolute top-0.5 size-5 rounded-full bg-surface shadow transition-all duration-200 ease-soft",
                  disponiblesSeulement ? "left-[1.375rem]" : "left-0.5",
                )}
              />
            </span>
            Seulement les livres disponibles
          </button>
          <p className="text-sm text-ink-muted" aria-hidden>
            {enAttente ? "Recherche…" : `${pluriel(affiches.length, "livre")} · ${pluriel(totalDisponibles, "exemplaire")} à emprunter`}
          </p>
        </div>
      </section>

      {pret && adherent && (
        <p className="-mt-4 text-sm text-ink-muted">
          Vous empruntez en tant que <strong className="font-semibold text-ink">{adherent.nom}</strong>.{" "}
          <Link href="/mon-compte" className="font-medium text-forest underline-offset-4 hover:underline">
            Voir mon compte
          </Link>
        </p>
      )}

      {erreur && (
        <Alerte
          ton="erreur"
          titre={erreur.titre}
          action={
            <Bouton variante="secondaire" compact onClick={recharger}>
              Réessayer
            </Bouton>
          }
        >
          {erreur.detail}
        </Alerte>
      )}

      {enAttente && !erreur && (
        <ul className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4" aria-label="Chargement du catalogue">
          {Array.from({ length: 8 }, (_, i) => (
            <li key={i} className="rounded-lg border border-line bg-surface p-4">
              <Squelette className="aspect-[3/4] w-full" />
              <Squelette className="mt-4 h-5 w-4/5" />
              <Squelette className="mt-2 h-4 w-1/2" />
              <Squelette className="mt-5 h-11 w-full" />
            </li>
          ))}
        </ul>
      )}

      {!enAttente && !erreur && affiches.length === 0 && (
        <EtatVide
          illustration={requete ? <SearchX className="size-9" /> : <LibraryBig className="size-9" />}
          titre={requete ? `Aucun livre pour « ${requete} »` : disponiblesSeulement ? "Tous les livres sont empruntés" : "Le catalogue est vide"}
          action={
            requete ? (
              <Bouton variante="secondaire" onClick={() => setSaisie("")}>
                Effacer la recherche
              </Bouton>
            ) : disponiblesSeulement ? (
              <Bouton variante="secondaire" onClick={() => setDisponiblesSeulement(false)}>
                Afficher aussi les livres empruntés
              </Bouton>
            ) : (
              <Link href="/bibliothecaire" className={classesBouton("primaire")}>
                Ajouter le premier livre <ArrowRight className="size-4" aria-hidden />
              </Link>
            )
          }
        >
          {requete
            ? "Vérifiez l'orthographe ou cherchez avec moins de mots : la recherche porte sur le titre et sur l'auteur."
            : disponiblesSeulement
              ? "Revenez plus tard, ou consultez la liste complète pour voir quand les exemplaires reviennent."
              : "Un bibliothécaire peut ajouter des livres et leurs exemplaires depuis l'espace bibliothécaire."}
        </EtatVide>
      )}

      {!enAttente && !erreur && affiches.length > 0 && (
        <ul
          className={cn(
            "grid grid-cols-1 gap-6 transition-opacity sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4",
            chargement && "opacity-60",
          )}
          aria-label="Livres du catalogue"
        >
          {affiches.map((livre, index) => (
            <CarteLivre
              key={livre.isbn}
              livre={livre}
              requete={requete}
              index={index}
              enCours={enCours === livre.isbn}
              aUnAdherent={Boolean(adherent)}
              onEmprunter={() => lancerEmprunt(livre)}
            />
          ))}
        </ul>
      )}

      {pret && !adherent && affiches.length > 0 && (
        <Alerte
          ton="info"
          titre="Connectez-vous pour emprunter"
          action={
            <Link href="/inscription" className={classesBouton("primaire", true)}>
              <BookCheck className="size-4" aria-hidden />
              Rejoindre la bibliothèque
            </Link>
          }
        >
          Inscrivez-vous en une minute ou ouvrez votre compte avec votre identifiant.
        </Alerte>
      )}
    </div>
  );
}

export default function PageCatalogue() {
  return (
    <Suspense fallback={<Squelette className="h-96 w-full" />}>
      <Catalogue />
    </Suspense>
  );
}
