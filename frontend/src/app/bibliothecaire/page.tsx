"use client";

import { BookPlus, LibraryBig, Plus, SearchX, Trash2 } from "lucide-react";
import { useCallback, useState, type FormEvent } from "react";
import { LivreCover } from "@/components/livre-cover";
import { useToast } from "@/components/providers";
import { BarreRecherche, Surbrillance } from "@/components/recherche";
import { Alerte, Badge, Bouton, Carte, Champ, EtatVide, Squelette, SurTitre, classesChamp } from "@/components/ui";
import { ApiError, ajouterLivre, listerLivres, retirerExemplaire } from "@/lib/api";
import { pluriel } from "@/lib/format";
import { exemplairesDisponibles, type Exemplaire, type Livre, type StatutExemplaire } from "@/lib/types";
import { useApi } from "@/lib/use-api";
import { useDebounce } from "@/lib/use-debounce";
import { cn } from "@/lib/utils";

const ETATS: Record<StatutExemplaire, { libelle: string; ton: "forest" | "gold" | "neutre" }> = {
  DISPONIBLE: { libelle: "Disponible", ton: "forest" },
  EMPRUNTE: { libelle: "Emprunté", ton: "gold" },
  RETIRE: { libelle: "Retiré", ton: "neutre" },
};

function Statistique({ valeur, libelle, ton }: { valeur: number; libelle: string; ton: string }) {
  return (
    <Carte className="p-5">
      <p className={cn("font-display text-4xl font-semibold leading-none", ton)}>{valeur}</p>
      <p className="mt-2 text-sm text-ink-muted">{libelle}</p>
    </Carte>
  );
}

function LigneExemplaire({ exemplaire, onRetire }: { exemplaire: Exemplaire; onRetire: () => void }) {
  const toast = useToast();
  const [confirmation, setConfirmation] = useState(false);
  const [envoi, setEnvoi] = useState(false);
  const { libelle, ton } = ETATS[exemplaire.statut];

  async function retirer() {
    setEnvoi(true);
    try {
      await retirerExemplaire(exemplaire.id);
      toast.succes(`L'exemplaire ${exemplaire.id} est retiré du catalogue.`, "Exemplaire retiré");
      onRetire();
    } catch (cause) {
      toast.erreur(cause);
      setConfirmation(false);
      onRetire();
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <li className="flex flex-wrap items-center justify-between gap-3 py-2.5">
      <div className="flex items-center gap-3">
        <span className="font-mono text-sm text-ink-muted">#{exemplaire.id}</span>
        <Badge ton={ton}>{libelle}</Badge>
      </div>
      {exemplaire.statut === "DISPONIBLE" &&
        (confirmation ? (
          <div className="flex items-center gap-2">
            <Bouton variante="danger" compact chargement={envoi} onClick={retirer}>
              Confirmer le retrait
            </Bouton>
            <Bouton variante="discret" compact onClick={() => setConfirmation(false)} disabled={envoi}>
              Annuler
            </Bouton>
          </div>
        ) : (
          <Bouton variante="discret" compact onClick={() => setConfirmation(true)} aria-label={`Retirer l'exemplaire ${exemplaire.id}`}>
            <Trash2 className="size-4" aria-hidden />
            Retirer
          </Bouton>
        ))}
      {exemplaire.statut === "EMPRUNTE" && <span className="text-xs text-ink-faint">À rendre avant retrait</span>}
    </li>
  );
}

function CarteLivreGestion({ livre, requete, onChange }: { livre: Livre; requete: string; onChange: () => void }) {
  const toast = useToast();
  const [envoi, setEnvoi] = useState(false);

  async function ajouter() {
    setEnvoi(true);
    try {
      await ajouterLivre(livre.isbn, livre.titre, livre.auteur, 1);
      toast.succes(`Un exemplaire de « ${livre.titre} » est ajouté et disponible.`, "Exemplaire ajouté");
      onChange();
    } catch (cause) {
      toast.erreur(cause);
    } finally {
      setEnvoi(false);
    }
  }

  const empruntes = livre.exemplaires.filter((e) => e.statut === "EMPRUNTE").length;

  return (
    <Carte className="p-5 sm:p-6">
      <div className="flex gap-5">
        <LivreCover titre={livre.titre} auteur={livre.auteur} compact className="hidden w-20 shrink-0 sm:block" />
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div className="min-w-0">
              <h3 className="font-display text-xl font-semibold leading-snug text-ink">
                <Surbrillance texte={livre.titre} requete={requete} />
              </h3>
              <p className="text-ink-muted">
                <Surbrillance texte={livre.auteur} requete={requete} />
              </p>
              <p className="mt-0.5 font-mono text-xs text-ink-faint">
                ISBN <Surbrillance texte={livre.isbn} requete={requete} />
              </p>
            </div>
            <Bouton variante="secondaire" compact chargement={envoi} onClick={ajouter}>
              <Plus className="size-4" aria-hidden />
              Ajouter un exemplaire
            </Bouton>
          </div>
          <p className="mt-3 text-sm text-ink-muted">
            {pluriel(livre.exemplaires.length, "exemplaire")} · {exemplairesDisponibles(livre)} disponible
            {exemplairesDisponibles(livre) > 1 ? "s" : ""} · {empruntes} emprunté{empruntes > 1 ? "s" : ""}
          </p>
          <ul className="mt-3 divide-y divide-line border-t border-line">
            {livre.exemplaires.map((exemplaire) => (
              <LigneExemplaire key={exemplaire.id} exemplaire={exemplaire} onRetire={onChange} />
            ))}
          </ul>
        </div>
      </div>
    </Carte>
  );
}

function FormulaireLivre({ onAjoute }: { onAjoute: () => void }) {
  const toast = useToast();
  const [isbn, setIsbn] = useState("");
  const [titre, setTitre] = useState("");
  const [auteur, setAuteur] = useState("");
  const [nombre, setNombre] = useState("1");
  const [touches, setTouches] = useState({ isbn: false, titre: false, auteur: false, nombre: false });
  const [erreurServeur, setErreurServeur] = useState<ApiError | null>(null);
  const [envoi, setEnvoi] = useState(false);

  const n = Number(nombre);
  const erreurs = {
    isbn: !isbn.trim()
      ? "L'ISBN du livre est obligatoire."
      : isbn.trim().length > 32
        ? "L'ISBN ne doit pas dépasser 32 caractères."
        : !/^[0-9A-Za-z-]+$/.test(isbn.trim())
          ? "L'ISBN ne peut contenir que des chiffres, des lettres et des tirets."
          : undefined,
    titre: !titre.trim() ? "Le titre du livre est obligatoire." : titre.trim().length > 255 ? "Le titre du livre ne doit pas dépasser 255 caractères." : undefined,
    auteur: !auteur.trim() ? "L'auteur du livre est obligatoire." : auteur.trim().length > 255 ? "Le nom de l'auteur ne doit pas dépasser 255 caractères." : undefined,
    nombre:
      nombre.trim() === ""
        ? "Le nombre d'exemplaires est obligatoire."
        : !Number.isInteger(n) || n < 1
          ? "Un livre doit avoir au moins 1 exemplaire."
          : n > 500
            ? "Un livre ne peut pas être créé avec plus de 500 exemplaires à la fois."
            : undefined,
  };
  const afficher = (champ: keyof typeof erreurs) =>
    erreurServeur?.pourChamp(champ) ?? (touches[champ] ? erreurs[champ] : undefined);

  function modifier(setter: (valeur: string) => void) {
    return (valeur: string) => {
      setter(valeur);
      setErreurServeur(null);
    };
  }

  async function soumettre(evenement: FormEvent) {
    evenement.preventDefault();
    setTouches({ isbn: true, titre: true, auteur: true, nombre: true });
    if (erreurs.isbn || erreurs.titre || erreurs.auteur || erreurs.nombre) return;
    setEnvoi(true);
    setErreurServeur(null);
    try {
      const livre = await ajouterLivre(isbn.trim(), titre.trim(), auteur.trim(), n);
      toast.succes(`« ${livre.titre} » est au catalogue avec ${pluriel(livre.exemplaires.length, "exemplaire")}.`, "Livre ajouté");
      setIsbn("");
      setTitre("");
      setAuteur("");
      setNombre("1");
      setTouches({ isbn: false, titre: false, auteur: false, nombre: false });
      onAjoute();
    } catch (cause) {
      if (cause instanceof ApiError && cause.erreurs.length > 0) setErreurServeur(cause);
      else toast.erreur(cause);
    } finally {
      setEnvoi(false);
    }
  }

  return (
    <Carte className="p-6">
      <div className="flex items-center gap-3">
        <span className="grid size-10 place-items-center rounded-lg bg-forest-soft text-forest" aria-hidden>
          <BookPlus className="size-5" />
        </span>
        <h2 className="font-display text-xl font-semibold text-ink">Ajouter un livre</h2>
      </div>
      <form onSubmit={soumettre} noValidate className="mt-6 flex flex-col gap-5">
        <Champ label="ISBN" erreur={afficher("isbn")} aide="Si l'ISBN est déjà au catalogue, les exemplaires s'ajoutent à ce livre.">
          {(props) => (
            <input {...props} value={isbn} onChange={(e) => modifier(setIsbn)(e.target.value)} onBlur={() => setTouches((t) => ({ ...t, isbn: true }))} placeholder="978-2-7087-0000-0" autoComplete="off" spellCheck={false} className={`${classesChamp} font-mono text-sm`} />
          )}
        </Champ>
        <Champ label="Titre" erreur={afficher("titre")}>
          {(props) => (
            <input {...props} value={titre} onChange={(e) => modifier(setTitre)(e.target.value)} onBlur={() => setTouches((t) => ({ ...t, titre: true }))} placeholder="Une si longue lettre" className={classesChamp} />
          )}
        </Champ>
        <Champ label="Auteur" erreur={afficher("auteur")}>
          {(props) => (
            <input {...props} value={auteur} onChange={(e) => modifier(setAuteur)(e.target.value)} onBlur={() => setTouches((t) => ({ ...t, auteur: true }))} placeholder="Mariama Bâ" className={classesChamp} />
          )}
        </Champ>
        <Champ label="Nombre d'exemplaires" erreur={afficher("nombre")} aide="Entre 1 et 500. Vous pourrez en ajouter plus tard avec le même ISBN.">
          {(props) => (
            <input {...props} type="number" inputMode="numeric" min={1} max={500} value={nombre} onChange={(e) => modifier(setNombre)(e.target.value)} onBlur={() => setTouches((t) => ({ ...t, nombre: true }))} className={classesChamp} />
          )}
        </Champ>
        <Bouton type="submit" chargement={envoi}>
          <Plus className="size-4" aria-hidden />
          Ajouter au catalogue
        </Bouton>
      </form>
    </Carte>
  );
}

export default function PageBibliothecaire() {
  const [saisie, setSaisie] = useState("");
  const requete = useDebounce(saisie.trim(), 300);

  const chargeurTous = useCallback(() => listerLivres(""), []);
  const chargeurRecherche = useCallback(() => listerLivres(requete), [requete]);
  const { donnees: tous, recharger: rechargerTous } = useApi(chargeurTous);
  const { donnees: resultats, erreur, chargement, recharger: rechargerRecherche } = useApi(chargeurRecherche);

  const actualiser = useCallback(() => {
    rechargerTous();
    rechargerRecherche();
  }, [rechargerTous, rechargerRecherche]);

  const exemplaires = (tous ?? []).flatMap((l) => l.exemplaires);
  const compter = (statut: StatutExemplaire) => exemplaires.filter((e) => e.statut === statut).length;
  const enAttente = chargement && resultats === null;
  const liste = resultats ?? [];

  return (
    <div className="flex flex-col gap-10">
      <header className="animate-rise">
        <SurTitre>Espace bibliothécaire</SurTitre>
        <h1 className="mt-3 text-balance font-display text-4xl font-semibold leading-tight text-ink sm:text-5xl">
          Gérez le catalogue, <span className="italic text-forest">un exemplaire à la fois.</span>
        </h1>
      </header>

      <section aria-label="Chiffres du catalogue" className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <Statistique valeur={tous?.length ?? 0} libelle="Livres au catalogue" ton="text-ink" />
        <Statistique valeur={compter("DISPONIBLE")} libelle="Exemplaires disponibles" ton="text-forest" />
        <Statistique valeur={compter("EMPRUNTE")} libelle="Exemplaires empruntés" ton="text-gold" />
        <Statistique valeur={compter("RETIRE")} libelle="Exemplaires retirés" ton="text-ink-faint" />
      </section>

      <div className="grid items-start gap-8 lg:grid-cols-[22rem_1fr]">
        <div className="lg:sticky lg:top-24">
          <FormulaireLivre onAjoute={actualiser} />
        </div>

        <section aria-labelledby="titre-liste" className="flex flex-col gap-5">
          <h2 id="titre-liste" className="sr-only">
            Livres du catalogue
          </h2>
          <BarreRecherche
            valeur={saisie}
            onChange={setSaisie}
            libelle="Rechercher un livre par titre, par auteur ou par ISBN"
            placeholder="Rechercher par titre, auteur ou ISBN"
            annonce={enAttente ? "Recherche en cours" : `${pluriel(liste.length, "livre")} ${liste.length > 1 ? "trouvés" : "trouvé"}`}
          />

          {erreur && (
            <Alerte
              ton="erreur"
              titre={erreur.titre}
              action={
                <Bouton variante="secondaire" compact onClick={actualiser}>
                  Réessayer
                </Bouton>
              }
            >
              {erreur.detail}
            </Alerte>
          )}

          {enAttente && !erreur && (
            <div className="flex flex-col gap-4" aria-label="Chargement des livres">
              <Squelette className="h-44" />
              <Squelette className="h-44" />
            </div>
          )}

          {!enAttente && !erreur && liste.length === 0 && (
            <EtatVide
              illustration={requete ? <SearchX className="size-9" /> : <LibraryBig className="size-9" />}
              titre={requete ? `Aucun livre pour « ${requete} »` : "Le catalogue est vide"}
              action={requete ? <Bouton variante="secondaire" onClick={() => setSaisie("")}>Effacer la recherche</Bouton> : undefined}
            >
              {requete ? "Cherchez avec moins de mots, ou ajoutez ce livre avec le formulaire." : "Ajoutez votre premier livre avec le formulaire : ses exemplaires seront aussitôt disponibles."}
            </EtatVide>
          )}

          {liste.length > 0 && (
            <ul className={cn("flex flex-col gap-4 transition-opacity", chargement && "opacity-60")}>
              {liste.map((livre) => (
                <li key={livre.isbn}>
                  <CarteLivreGestion livre={livre} requete={requete} onChange={actualiser} />
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
