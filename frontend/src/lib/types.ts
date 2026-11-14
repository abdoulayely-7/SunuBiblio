export type StatutExemplaire = "DISPONIBLE" | "EMPRUNTE" | "RETIRE";
export type TypeAdherent = "ETUDIANT" | "ENSEIGNANT";
export type StatutPenalite = "IMPAYEE" | "REGLEE";
export type StatutEmprunt = "EN_COURS" | "RESTITUE";

export interface Exemplaire {
  id: string;
  livreIsbn: string;
  statut: StatutExemplaire;
}

/** Un livre du catalogue, identifie par son ISBN. */
export interface Livre {
  isbn: string;
  titre: string;
  auteur: string;
  exemplaires: Exemplaire[];
}

export interface Adherent {
  id: string;
  nom: string;
  type: TypeAdherent;
  quotaEmprunts: number;
  estBloquePourImpayes: boolean;
  montantImpaye: number;
}

export interface Emprunt {
  id: string;
  adherentId: string;
  exemplaireId: string;
  dateEmprunt: string;
  dateEcheance: string;
  dateRetour: string | null;
  statut: StatutEmprunt;
}

export interface Penalite {
  id: string;
  adherentId: string;
  exemplaireId: string;
  joursDeRetard: number;
  montant: number;
  statut: StatutPenalite;
  dateCreation: string;
}

export interface Restitution {
  emprunt: Emprunt;
  joursDeRetard: number;
  penalite: Penalite | null;
}

/** Ce que « Mon compte » affiche : l'adherent, ses emprunts en cours et toutes ses penalites. */
export interface Situation {
  adherent: Adherent;
  empruntsEnCours: Emprunt[];
  penalites: Penalite[];
}

/** Un champ de formulaire refuse par l'API (liste `erreurs` du format Problem Details). */
export interface ErreurChamp {
  champ: string;
  message: string;
}

/** Nombre d'exemplaires qu'on peut emprunter tout de suite. */
export function exemplairesDisponibles(livre: Livre): number {
  return livre.exemplaires.filter((e) => e.statut === "DISPONIBLE").length;
}
