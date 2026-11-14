import type { TypeAdherent } from "./types";

const MS_PAR_JOUR = 24 * 60 * 60 * 1000;

function enDate(iso: string): Date {
  const [annee, mois, jour] = iso.split("-").map(Number);
  return new Date(annee, mois - 1, jour);
}

/** « 2026-11-28 » devient « 28 novembre 2026 ». */
export function formaterDate(iso: string): string {
  return new Intl.DateTimeFormat("fr-FR", { day: "numeric", month: "long", year: "numeric" }).format(enDate(iso));
}

/** Nombre de jours entre aujourd'hui et la date (negatif si la date est passee). */
export function joursAvant(iso: string, aujourdhui: Date = new Date()): number {
  const debutDuJour = new Date(aujourdhui.getFullYear(), aujourdhui.getMonth(), aujourdhui.getDate());
  return Math.round((enDate(iso).getTime() - debutDuJour.getTime()) / MS_PAR_JOUR);
}

export function pluriel(nombre: number, singulier: string, plurielForme = `${singulier}s`): string {
  return `${nombre} ${Math.abs(nombre) > 1 ? plurielForme : singulier}`;
}

export function formaterXof(montant: number): string {
  return `${new Intl.NumberFormat("fr-FR").format(montant)} XOF`;
}

export function libelleType(type: TypeAdherent): string {
  return type === "ENSEIGNANT" ? "Enseignant" : "Étudiant";
}

export function initiales(nom: string): string {
  return (
    nom
      .trim()
      .split(/\s+/)
      .slice(0, 2)
      .map((mot) => mot[0]?.toUpperCase() ?? "")
      .join("") || "?"
  );
}

/** Un identifiant d'adherent : « ADH- » suivi de 8 caracteres hexadecimaux. */
export const ID_ADHERENT_VALIDE = /^ADH-[0-9A-F]{8}$/i;
