import type {
  Adherent,
  Emprunt,
  ErreurChamp,
  Livre,
  Penalite,
  Restitution,
  Situation,
  TypeAdherent,
} from "./types";

/**
 * Une erreur de l'API, toujours exploitable par l'interface : un titre court, un message explicite
 * a afficher tel quel, les champs fautifs (400) et l'identifiant de suivi (500, 503).
 */
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly titre: string,
    public readonly detail: string,
    public readonly erreurs: ErreurChamp[] = [],
    public readonly traceId?: string,
  ) {
    super(detail);
    this.name = "ApiError";
  }

  /** Le message de l'API pour un champ de formulaire, s'il y en a un. */
  pourChamp(champ: string): string | undefined {
    return this.erreurs.find((e) => e.champ === champ)?.message;
  }

  get estConflit() {
    return this.status === 409;
  }

  get estPanneServeur() {
    return this.status === 0 || this.status >= 500;
  }
}

/** Message affichable pour n'importe quelle erreur, y compris une erreur qui n'est pas de l'API. */
export function messageDepuis(erreur: unknown): { titre: string; detail: string; traceId?: string } {
  if (erreur instanceof ApiError) {
    return { titre: erreur.titre, detail: erreur.detail, traceId: erreur.traceId };
  }
  return {
    titre: "Erreur inattendue",
    detail: "Une erreur inattendue s'est produite dans l'application. Rechargez la page et réessayez.",
  };
}

async function erreurDepuis(reponse: Response): Promise<ApiError> {
  let corps: unknown = null;
  try {
    corps = await reponse.json();
  } catch {
    corps = null;
  }

  if (corps && typeof corps === "object" && "detail" in corps && typeof corps.detail === "string") {
    const pd = corps as { title?: string; detail: string; erreurs?: ErreurChamp[]; traceId?: string };
    return new ApiError(reponse.status, pd.title ?? "Erreur", pd.detail, pd.erreurs ?? [], pd.traceId);
  }

  if (reponse.status >= 500) {
    return new ApiError(
      reponse.status,
      "Serveur indisponible",
      `Le serveur SunuBiblio ne répond pas correctement (code ${reponse.status}). Vérifiez qu'il est démarré, puis réessayez.`,
    );
  }
  return new ApiError(reponse.status, "Requête refusée", `La requête a échoué (code ${reponse.status}).`);
}

async function appeler<T>(chemin: string, init?: RequestInit): Promise<T> {
  let reponse: Response;
  try {
    reponse = await fetch(`/api${chemin}`, {
      ...init,
      headers: { Accept: "application/json", "Content-Type": "application/json", ...init?.headers },
      cache: "no-store",
    });
  } catch {
    throw new ApiError(
      0,
      "Serveur injoignable",
      "Impossible de joindre le serveur SunuBiblio. Vérifiez votre connexion et que l'API est démarrée, puis réessayez.",
    );
  }

  if (!reponse.ok) {
    throw await erreurDepuis(reponse);
  }
  if (reponse.status === 204) {
    return undefined as T;
  }
  return (await reponse.json()) as T;
}

const corps = (donnees: unknown) => JSON.stringify(donnees);
const id = (valeur: string) => encodeURIComponent(valeur);

export const listerLivres = (recherche = "") =>
  appeler<Livre[]>(`/catalogue/livres${recherche.trim() ? `?q=${encodeURIComponent(recherche.trim())}` : ""}`);

/** Avec un ISBN deja au catalogue, les exemplaires s'ajoutent a ce livre (titre et auteur inchanges). */
export const ajouterLivre = (isbn: string, titre: string, auteur: string, nombreExemplaires: number) =>
  appeler<Livre>("/catalogue/livres", { method: "POST", body: corps({ isbn, titre, auteur, nombreExemplaires }) });

export const retirerExemplaire = (exemplaireId: string) =>
  appeler<void>(`/catalogue/exemplaires/${id(exemplaireId)}`, { method: "DELETE" });

export const inscrire = (nom: string, type: TypeAdherent) =>
  appeler<Adherent>("/adherents", { method: "POST", body: corps({ nom, type }) });

export const trouverAdherent = (adherentId: string) => appeler<Adherent>(`/adherents/${id(adherentId)}`);

export const emprunter = (adherentId: string, exemplaireId: string) =>
  appeler<Emprunt>("/prets", { method: "POST", body: corps({ adherentId, exemplaireId }) });

export const rendre = (empruntId: string, dateRetour?: string) =>
  appeler<Restitution>(`/prets/${id(empruntId)}/restitution`, {
    method: "POST",
    body: dateRetour ? corps({ dateRetour }) : undefined,
  });

/** L'adherent, ses emprunts en cours et ses penalites : trois appels, reunis pour l'ecran « Mon compte ». */
export async function situationDe(adherentId: string): Promise<Situation> {
  const adherent = await trouverAdherent(adherentId);
  const [empruntsEnCours, penalites] = await Promise.all([
    appeler<Emprunt[]>(`/prets?adherentId=${id(adherentId)}`),
    appeler<Penalite[]>(`/penalites?adherentId=${id(adherentId)}`),
  ]);
  return { adherent, empruntsEnCours, penalites };
}

export const payerPenalite = (penaliteId: string) =>
  appeler<void>(`/penalites/${id(penaliteId)}/payer`, { method: "POST" });
