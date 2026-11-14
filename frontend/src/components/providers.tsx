"use client";

import { CircleCheck, Info, TriangleAlert, X } from "lucide-react";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { messageDepuis } from "@/lib/api";
import { ecrire, useHydrate, useStockage } from "@/lib/stockage";
import type { Adherent } from "@/lib/types";
import { cn } from "@/lib/utils";

/* ------------------------------ Notifications ------------------------------ */

type TonToast = "succes" | "erreur" | "info";

interface Toast {
  id: number;
  ton: TonToast;
  titre: string;
  message: string;
  traceId?: string;
}

interface ToastApi {
  succes: (message: string, titre?: string) => void;
  info: (message: string, titre?: string) => void;
  /** Affiche le titre et le message explicite de l'API (et l'identifiant de suivi des pannes). */
  erreur: (erreur: unknown) => void;
}

const ToastContext = createContext<ToastApi | null>(null);

const STYLES: Record<TonToast, { barre: string; icone: string; Icone: typeof Info }> = {
  succes: { barre: "bg-forest", icone: "text-forest", Icone: CircleCheck },
  erreur: { barre: "bg-terracotta", icone: "text-terracotta", Icone: TriangleAlert },
  info: { barre: "bg-gold", icone: "text-gold", Icone: Info },
};

function ToastCarte({ toast, fermer }: { toast: Toast; fermer: () => void }) {
  const { barre, icone, Icone } = STYLES[toast.ton];

  useEffect(() => {
    const delai = window.setTimeout(fermer, toast.ton === "erreur" ? 12000 : 6000);
    return () => window.clearTimeout(delai);
  }, [fermer, toast.ton]);

  return (
    <div
      role={toast.ton === "erreur" ? "alert" : "status"}
      className="animate-pop relative flex gap-3 overflow-hidden rounded-lg border border-line bg-surface p-4 pl-5 shadow-lift"
    >
      <span className={cn("absolute inset-y-0 left-0 w-1.5", barre)} aria-hidden />
      <Icone className={cn("mt-0.5 size-5 shrink-0", icone)} aria-hidden />
      <div className="min-w-0 flex-1">
        <p className="font-display text-base font-semibold leading-snug text-ink">{toast.titre}</p>
        <p className="mt-1 text-sm leading-relaxed text-ink-muted">{toast.message}</p>
        {toast.traceId && (
          <p className="mt-2 text-xs text-ink-faint">
            Identifiant de suivi : <span className="font-mono text-ink-muted">{toast.traceId}</span>
          </p>
        )}
      </div>
      <button
        type="button"
        onClick={fermer}
        aria-label="Fermer la notification"
        className="-mr-1 -mt-1 grid size-9 shrink-0 place-items-center rounded-md text-ink-faint transition-colors hover:bg-paper-deep hover:text-ink"
      >
        <X className="size-4" aria-hidden />
      </button>
    </div>
  );
}

function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const compteur = useRef(0);

  const fermer = useCallback((id: number) => setToasts((liste) => liste.filter((t) => t.id !== id)), []);
  const ajouter = useCallback((toast: Omit<Toast, "id">) => {
    compteur.current += 1;
    const id = compteur.current;
    setToasts((liste) => [...liste.slice(-3), { ...toast, id }]);
  }, []);

  const api = useMemo<ToastApi>(
    () => ({
      succes: (message, titre = "C'est fait") => ajouter({ ton: "succes", titre, message }),
      info: (message, titre = "À savoir") => ajouter({ ton: "info", titre, message }),
      erreur: (erreur) => {
        const { titre, detail, traceId } = messageDepuis(erreur);
        ajouter({ ton: "erreur", titre, message: detail, traceId });
      },
    }),
    [ajouter],
  );

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div
        aria-live="polite"
        className="pointer-events-none fixed inset-x-4 bottom-24 z-50 flex flex-col gap-3 sm:inset-x-auto sm:bottom-6 sm:right-6 sm:w-[26rem] md:bottom-6"
      >
        {toasts.map((toast) => (
          <div key={toast.id} className="pointer-events-auto">
            <ToastCarte toast={toast} fermer={() => fermer(toast.id)} />
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast(): ToastApi {
  const contexte = useContext(ToastContext);
  if (!contexte) throw new Error("useToast doit etre utilise sous <Providers>.");
  return contexte;
}

/* ------------------------------ Adherent actif ------------------------------ */

const CLE_ACTIF = "sunubiblio.adherent";
const CLE_CONNUS = "sunubiblio.adherents";

interface AdherentApi {
  /** Faux tant que le navigateur n'a pas relu la memoire locale : evite d'afficher « aucun adherent » trop tot. */
  pret: boolean;
  adherent: Adherent | null;
  connus: Adherent[];
  choisir: (adherent: Adherent) => void;
  seDeconnecter: () => void;
  oublier: (adherentId: string) => void;
}

const AdherentContext = createContext<AdherentApi | null>(null);

function analyser<T>(brut: string | null, parDefaut: T): T {
  if (!brut) return parDefaut;
  try {
    return JSON.parse(brut) as T;
  } catch {
    return parDefaut;
  }
}

function AdherentProvider({ children }: { children: ReactNode }) {
  const pret = useHydrate();
  const brutActif = useStockage(CLE_ACTIF);
  const brutConnus = useStockage(CLE_CONNUS);

  const adherent = useMemo(() => analyser<Adherent | null>(brutActif, null), [brutActif]);
  const connus = useMemo(() => analyser<Adherent[]>(brutConnus, []), [brutConnus]);

  const api = useMemo<AdherentApi>(
    () => ({
      pret,
      adherent,
      connus,
      choisir: (nouveau) => {
        ecrire(CLE_ACTIF, JSON.stringify(nouveau));
        ecrire(CLE_CONNUS, JSON.stringify([nouveau, ...connus.filter((a) => a.id !== nouveau.id)]));
      },
      seDeconnecter: () => ecrire(CLE_ACTIF, null),
      oublier: (adherentId) => {
        ecrire(CLE_CONNUS, JSON.stringify(connus.filter((a) => a.id !== adherentId)));
        if (adherent?.id === adherentId) ecrire(CLE_ACTIF, null);
      },
    }),
    [pret, adherent, connus],
  );

  return <AdherentContext.Provider value={api}>{children}</AdherentContext.Provider>;
}

export function useAdherent(): AdherentApi {
  const contexte = useContext(AdherentContext);
  if (!contexte) throw new Error("useAdherent doit etre utilise sous <Providers>.");
  return contexte;
}

export function Providers({ children }: { children: ReactNode }) {
  return (
    <ToastProvider>
      <AdherentProvider>{children}</AdherentProvider>
    </ToastProvider>
  );
}
