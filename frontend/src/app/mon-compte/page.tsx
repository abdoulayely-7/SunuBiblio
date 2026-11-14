"use client";

import { BookOpen, CalendarClock, CalendarDays, Coins, Copy, RotateCcw, ShieldAlert, UserRound } from "lucide-react";
import Link from "next/link";
import { useCallback, useMemo, useState } from "react";
import { LivreCover } from "@/components/livre-cover";
import { useAdherent, useToast } from "@/components/providers";
import { QuotaRing } from "@/components/quota-ring";
import { Alerte, Badge, Bouton, Carte, EtatVide, Squelette, SurTitre, classesBouton } from "@/components/ui";
import { ApiError, listerLivres, payerPenalite, rendre, situationDe } from "@/lib/api";
import { formaterDate, formaterXof, joursAvant, libelleType, pluriel } from "@/lib/format";
import type { Emprunt, Livre, Penalite, Situation } from "@/lib/types";
import { useApi } from "@/lib/use-api";
import { cn } from "@/lib/utils";

function LigneEmprunt({
  emprunt,
  livre,
  onRendu,
}: {
  emprunt: Emprunt;
  livre: Livre | undefined;
  onRendu: () => void;
}) {
  const toast = useToast();
  const [dateChoisie, setDateChoisie] = useState<string | null>(null);
  const [envoi, setEnvoi] = useState(false);

  const jours = joursAvant(emprunt.dateEcheance);
  const enRetard = jours < 0;
  const titre = livre?.titre ?? "Livre du catalogue";

  async function lancerRetour() {
    setEnvoi(true);
    try {
      const retour = await rendre(emprunt.id, dateChoisie || undefined);
      if (retour.penalite) {
        toast.erreur(
          new ApiError(
            409,
            `Rendu avec ${pluriel(retour.joursDeRetard, "jour")} de retard`,
            `« ${titre} » est de retour, mais une pénalité de ${formaterXof(retour.penalite.montant)} a été créée. Payez-la pour pouvoir emprunter de nouveau.`,
          ),
        );
      } else {
        toast.succes(`« ${titre} » est de retour à la bibliothèque, merci !`, "Rendu à temps");
      }
      onRendu();
    } catch (cause) {
      toast.erreur(cause);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <li className="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:p-5">
      <div className="flex min-w-0 flex-1 items-center gap-4">
        {livre ? (
          <LivreCover titre={livre.titre} auteur={livre.auteur} compact className="w-16 shrink-0" />
        ) : (
          <div className="grid aspect-[3/4] w-16 shrink-0 place-items-center rounded-md bg-paper-deep text-ink-faint" aria-hidden>
            <BookOpen className="size-6" />
          </div>
        )}
        <div className="min-w-0">
          <p className="truncate font-display text-lg font-semibold text-ink">{titre}</p>
          {livre && <p className="truncate text-sm text-ink-muted">{livre.auteur}</p>}
          <p className="mt-1 text-sm text-ink-faint">
            Emprunté le {formaterDate(emprunt.dateEmprunt)} · à rendre le {formaterDate(emprunt.dateEcheance)}
          </p>
          <div className="mt-2">
            {enRetard ? (
              <Badge ton="terracotta">En retard de {pluriel(-jours, "jour")}</Badge>
            ) : jours === 0 ? (
              <Badge ton="gold">À rendre aujourd&apos;hui</Badge>
            ) : (
              <Badge ton="forest">À rendre dans {pluriel(jours, "jour")}</Badge>
            )}
          </div>
        </div>
      </div>

      <div className="flex flex-col items-stretch gap-2 sm:items-end">
        {dateChoisie !== null && (
          <label className="flex flex-col gap-1 text-xs text-ink-muted">
            Date de retour (démonstration)
            <input
              type="date"
              value={dateChoisie}
              min={emprunt.dateEmprunt}
              onChange={(e) => setDateChoisie(e.target.value)}
              className="h-11 rounded-md border border-line-strong bg-surface px-3 text-sm text-ink focus:border-forest focus:outline-none focus:ring-2 focus:ring-forest/20"
            />
          </label>
        )}
        <div className="flex gap-2">
          <Bouton onClick={lancerRetour} chargement={envoi} compact>
            <RotateCcw className="size-4" aria-hidden />
            Rendre
          </Bouton>
          <Bouton
            variante="discret"
            compact
            aria-pressed={dateChoisie !== null}
            title="Rendre à une autre date, pour simuler un retard sans attendre 14 jours"
            onClick={() => setDateChoisie(dateChoisie === null ? emprunt.dateEcheance : null)}
          >
            <CalendarDays className="size-4" aria-hidden />
            <span className="sr-only sm:not-sr-only">{dateChoisie === null ? "Choisir une date" : "Aujourd'hui"}</span>
          </Bouton>
        </div>
      </div>
    </li>
  );
}

function LignePenalite({ penalite, onPayee }: { penalite: Penalite; onPayee: () => void }) {
  const toast = useToast();
  const [envoi, setEnvoi] = useState(false);
  const impayee = penalite.statut === "IMPAYEE";

  async function payer() {
    setEnvoi(true);
    try {
      await payerPenalite(penalite.id);
      toast.succes(`Votre pénalité de ${formaterXof(penalite.montant)} est payée. Vous pouvez emprunter de nouveau.`, "Paiement enregistré");
      onPayee();
    } catch (cause) {
      toast.erreur(cause);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <li className="flex flex-col gap-3 p-4 sm:flex-row sm:items-center sm:justify-between sm:p-5">
      <div>
        <p className="font-display text-lg font-semibold text-ink">
          {formaterXof(penalite.montant)}
          <span className="ml-2 text-sm font-normal text-ink-muted">· {pluriel(penalite.joursDeRetard, "jour")} de retard</span>
        </p>
        <p className="mt-1 text-sm text-ink-faint">Exemplaire {penalite.exemplaireId}</p>
      </div>
      {impayee ? (
        <div className="flex items-center gap-3">
          <Badge ton="terracotta">Impayée</Badge>
          <Bouton variante="danger" compact onClick={payer} chargement={envoi}>
            <Coins className="size-4" aria-hidden />
            Payer {formaterXof(penalite.montant)}
          </Bouton>
        </div>
      ) : (
        <Badge ton="forest">Payée</Badge>
      )}
    </li>
  );
}

function Contenu({ adherentId }: { adherentId: string }) {
  const { adherent, oublier } = useAdherent();

  const chargeurSituation = useCallback(() => situationDe(adherentId), [adherentId]);
  const chargeurLivres = useCallback(() => listerLivres(""), []);
  const { donnees: situation, erreur, chargement, recharger } = useApi<Situation>(chargeurSituation);
  const { donnees: livres, recharger: rechargerLivres } = useApi<Livre[]>(chargeurLivres);

  const livreParExemplaire = useMemo(() => {
    const index = new Map<string, Livre>();
    for (const livre of livres ?? []) for (const exemplaire of livre.exemplaires) index.set(exemplaire.id, livre);
    return index;
  }, [livres]);

  const actualiser = useCallback(() => {
    recharger();
    rechargerLivres();
  }, [recharger, rechargerLivres]);

  if (!adherent) return null;

  if (erreur) {
    return (
      <Alerte
        ton="erreur"
        titre={erreur.titre}
        action={
          <div className="flex gap-2">
            <Bouton variante="secondaire" compact onClick={recharger}>
              Réessayer
            </Bouton>
            {erreur.status === 404 && (
              <Bouton variante="discret" compact onClick={() => oublier(adherent.id)}>
                Retirer cet adhérent
              </Bouton>
            )}
          </div>
        }
      >
        {erreur.detail}
        {erreur.status === 404 && " Si la base a été réinitialisée, inscrivez-vous à nouveau."}
      </Alerte>
    );
  }

  if (chargement || !situation) {
    return (
      <div className="grid gap-6 lg:grid-cols-3" aria-label="Chargement de votre compte">
        <Squelette className="h-52" />
        <Squelette className="h-52" />
        <Squelette className="h-52" />
        <Squelette className="h-64 lg:col-span-3" />
      </div>
    );
  }

  // Le blocage vient de l'API (l'adherent l'apprend par l'evenement de penalite), pas d'un calcul ici.
  const bloque = situation.adherent.estBloquePourImpayes;
  const totalImpaye = situation.adherent.montantImpaye;
  const nombreImpayees = situation.penalites.filter((p) => p.statut === "IMPAYEE").length;
  const prochaine = [...situation.empruntsEnCours].sort((a, b) => a.dateEcheance.localeCompare(b.dateEcheance))[0];
  const utilises = situation.empruntsEnCours.length;

  return (
    <div className="flex flex-col gap-10">
      {bloque && (
        <Alerte
          ton="erreur"
          titre="Vous ne pouvez plus emprunter"
          action={
            <a href="#penalites" className={classesBouton("danger", true)}>
              Voir mes pénalités
            </a>
          }
        >
          Vous avez {formaterXof(totalImpaye)} de pénalités impayées. Payez-les pour retrouver le droit d&apos;emprunter ; vous pouvez
          toujours rendre vos livres.
        </Alerte>
      )}

      <section className="grid gap-6 lg:grid-cols-3" aria-label="Résumé de votre compte">
        <Carte className="flex items-center gap-6 p-6">
          <QuotaRing utilises={utilises} quota={adherent.quotaEmprunts} />
          <div>
            <p className="font-display text-xl font-semibold text-ink">Vos emprunts</p>
            <p className="mt-1 text-sm leading-relaxed text-ink-muted">
              {utilises >= adherent.quotaEmprunts
                ? "Vous avez atteint votre maximum : rendez un livre pour en emprunter un autre."
                : `Encore ${pluriel(adherent.quotaEmprunts - utilises, "livre")} possible${adherent.quotaEmprunts - utilises > 1 ? "s" : ""}.`}
            </p>
          </div>
        </Carte>

        <Carte className="flex flex-col justify-between p-6">
          <div className="flex items-center gap-3 text-ink-muted">
            <CalendarClock className="size-5 text-forest" aria-hidden />
            <p className="text-sm font-medium">Prochaine échéance</p>
          </div>
          {prochaine ? (
            <div className="mt-4">
              <p className="font-display text-3xl font-semibold leading-tight text-ink">{formaterDate(prochaine.dateEcheance)}</p>
              <p className="mt-2 text-sm text-ink-muted">
                {livreParExemplaire.get(prochaine.exemplaireId)?.titre ?? "Un de vos livres"} ·{" "}
                {joursAvant(prochaine.dateEcheance) < 0
                  ? `en retard de ${pluriel(-joursAvant(prochaine.dateEcheance), "jour")}`
                  : `dans ${pluriel(joursAvant(prochaine.dateEcheance), "jour")}`}
              </p>
            </div>
          ) : (
            <p className="mt-4 text-[0.9375rem] leading-relaxed text-ink-muted">Aucun livre à rendre pour le moment.</p>
          )}
        </Carte>

        <Carte className={cn("flex flex-col justify-between p-6", totalImpaye > 0 && "border-terracotta/40 bg-terracotta-soft/50")}>
          <div className="flex items-center gap-3 text-ink-muted">
            <ShieldAlert className={cn("size-5", totalImpaye > 0 ? "text-terracotta" : "text-forest")} aria-hidden />
            <p className="text-sm font-medium">Pénalités à payer</p>
          </div>
          <div className="mt-4">
            <p className={cn("font-display text-3xl font-semibold leading-tight", totalImpaye > 0 ? "text-terracotta-deep" : "text-ink")}>
              {formaterXof(totalImpaye)}
            </p>
            <p className="mt-2 text-sm text-ink-muted">
              {totalImpaye > 0 ? `${pluriel(nombreImpayees, "pénalité")} en attente de paiement.` : "Tout est à jour, merci !"}
            </p>
          </div>
        </Carte>
      </section>

      <section aria-labelledby="titre-emprunts">
        <h2 id="titre-emprunts" className="font-display text-2xl font-semibold text-ink">
          Emprunts en cours
        </h2>
        {situation.empruntsEnCours.length === 0 ? (
          <div className="mt-4">
            <EtatVide
              illustration={<BookOpen className="size-9" />}
              titre="Aucun emprunt en cours"
              action={
                <Link href="/" className={classesBouton("primaire")}>
                  Parcourir le catalogue
                </Link>
              }
            >
              Choisissez un livre dans le catalogue : il sera à vous pour 14 jours.
            </EtatVide>
          </div>
        ) : (
          <Carte className="mt-4 divide-y divide-line overflow-hidden">
            <ul className="divide-y divide-line">
              {situation.empruntsEnCours.map((emprunt) => (
                <LigneEmprunt key={emprunt.id} emprunt={emprunt} livre={livreParExemplaire.get(emprunt.exemplaireId)} onRendu={actualiser} />
              ))}
            </ul>
          </Carte>
        )}
      </section>

      <section id="penalites" aria-labelledby="titre-penalites" className="scroll-mt-24">
        <h2 id="titre-penalites" className="font-display text-2xl font-semibold text-ink">
          Pénalités
        </h2>
        {situation.penalites.length === 0 ? (
          <p className="mt-3 rounded-lg border border-line bg-surface p-5 text-ink-muted">
            Vous n&apos;avez aucune pénalité. Un retour après l&apos;échéance en crée une, à raison de 100 XOF par jour de retard.
          </p>
        ) : (
          <Carte className="mt-4 overflow-hidden">
            <ul className="divide-y divide-line">
              {situation.penalites.map((penalite) => (
                <LignePenalite key={penalite.id} penalite={penalite} onPayee={actualiser} />
              ))}
            </ul>
          </Carte>
        )}
      </section>
    </div>
  );
}

export default function PageMonCompte() {
  const { pret, adherent } = useAdherent();
  const toast = useToast();

  if (!pret) {
    return <Squelette className="h-64 w-full" />;
  }

  if (!adherent) {
    return (
      <EtatVide
        illustration={<UserRound className="size-9" />}
        titre="Aucun adhérent sélectionné"
        action={
          <Link href="/inscription" className={classesBouton("primaire")}>
            Créer ou ouvrir un compte
          </Link>
        }
      >
        Pour voir vos emprunts, rendre un livre ou payer une pénalité, rejoignez la bibliothèque ou ouvrez votre compte avec votre
        identifiant.
      </EtatVide>
    );
  }

  async function copierIdentifiant() {
    if (!adherent) return;
    try {
      await navigator.clipboard.writeText(adherent.id);
      toast.succes("Votre identifiant d'adhérent est dans le presse-papiers.", "Identifiant copié");
    } catch {
      toast.info(`Votre identifiant : ${adherent.id}`, "Copie impossible");
    }
  }

  return (
    <div className="flex flex-col gap-10">
      <header className="animate-rise flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <SurTitre>Mon compte</SurTitre>
          <h1 className="mt-3 text-balance font-display text-4xl font-semibold leading-tight text-ink sm:text-5xl">
            Bonjour, <span className="italic text-forest">{adherent.nom.split(" ")[0]}</span>
          </h1>
          <div className="mt-4 flex flex-wrap items-center gap-3">
            <Badge ton={adherent.type === "ENSEIGNANT" ? "gold" : "forest"}>{libelleType(adherent.type)}</Badge>
            <span className="text-sm text-ink-muted">{pluriel(adherent.quotaEmprunts, "livre")} à la fois, pour 14 jours</span>
          </div>
        </div>
        <Bouton variante="secondaire" compact onClick={copierIdentifiant} title={adherent.id}>
          <Copy className="size-4" aria-hidden />
          Identifiant {adherent.id}
        </Bouton>
      </header>

      <Contenu adherentId={adherent.id} />
    </div>
  );
}
