import { CircleCheck, Info, LoaderCircle, TriangleAlert } from "lucide-react";
import {
  useId,
  type ButtonHTMLAttributes,
  type HTMLAttributes,
  type ReactNode,
} from "react";
import { cn } from "@/lib/utils";

/* ---------------------------------- Bouton ---------------------------------- */

type Variante = "primaire" | "secondaire" | "discret" | "danger";

const VARIANTES: Record<Variante, string> = {
  primaire:
    "bg-forest text-surface shadow-[0_1px_0_rgb(255_253_249/0.14)_inset,0_8px_16px_-8px_rgb(30_77_59/0.7)] hover:-translate-y-px hover:bg-forest-deep active:translate-y-0",
  secondaire:
    "border border-line-strong bg-surface text-ink hover:border-forest hover:text-forest-deep",
  discret: "text-ink-muted hover:bg-paper-deep hover:text-ink",
  danger: "bg-terracotta text-surface hover:-translate-y-px hover:bg-terracotta-deep active:translate-y-0",
};

interface BoutonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: Variante;
  compact?: boolean;
  chargement?: boolean;
}

export function classesBouton(variante: Variante = "primaire", compact = false) {
  return cn(
    "inline-flex select-none items-center justify-center gap-2 rounded-md font-medium whitespace-nowrap transition-all duration-200 ease-soft disabled:pointer-events-none disabled:opacity-50",
    compact ? "h-11 px-4 text-sm sm:h-10" : "h-11 px-5 text-[0.9375rem]",
    VARIANTES[variante],
  );
}

export function Bouton({
  variante = "primaire",
  compact = false,
  chargement = false,
  disabled,
  className,
  children,
  type = "button",
  ...props
}: BoutonProps) {
  return (
    <button
      type={type}
      disabled={disabled || chargement}
      aria-busy={chargement || undefined}
      className={cn(classesBouton(variante, compact), className)}
      {...props}
    >
      {chargement && <LoaderCircle className="size-4 animate-spin" aria-hidden />}
      {children}
    </button>
  );
}

/* ---------------------------------- Champs ---------------------------------- */

export const classesChamp = cn(
  "h-11 w-full rounded-md border border-line-strong bg-surface px-3.5 text-[0.9375rem] text-ink transition-colors",
  "placeholder:text-ink-faint hover:border-ink-faint",
  "focus:border-forest focus:outline-none focus:ring-2 focus:ring-forest/20",
  "aria-[invalid=true]:border-terracotta aria-[invalid=true]:focus:ring-terracotta/20",
);

interface ChampProps {
  label: string;
  aide?: string;
  erreur?: string;
  className?: string;
  children: (props: { id: string; "aria-describedby": string | undefined; "aria-invalid": boolean }) => ReactNode;
}

/** Un champ de formulaire accessible : libelle, aide, et message d'erreur relie au champ. */
export function Champ({ label, aide, erreur, className, children }: ChampProps) {
  const id = useId();
  const idMessage = `${id}-message`;
  const message = erreur ?? aide;

  return (
    <div className={cn("flex flex-col gap-1.5", className)}>
      <label htmlFor={id} className="text-sm font-medium text-ink">
        {label}
      </label>
      {children({
        id,
        "aria-describedby": message ? idMessage : undefined,
        "aria-invalid": Boolean(erreur),
      })}
      {message && (
        <p
          id={idMessage}
          role={erreur ? "alert" : undefined}
          className={cn("text-sm leading-snug", erreur ? "text-terracotta-deep" : "text-ink-faint")}
        >
          {message}
        </p>
      )}
    </div>
  );
}

/* ----------------------------- Badge, carte, etc. ----------------------------- */

type Ton = "neutre" | "forest" | "terracotta" | "gold";

const TONS: Record<Ton, string> = {
  neutre: "bg-paper-deep text-ink-muted",
  forest: "bg-forest-soft text-forest-deep",
  terracotta: "bg-terracotta-soft text-terracotta-deep",
  gold: "bg-gold-soft text-gold",
};

export function Badge({
  ton = "neutre",
  className,
  children,
  ...props
}: HTMLAttributes<HTMLSpanElement> & { ton?: Ton }) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold tracking-wide",
        TONS[ton],
        className,
      )}
      {...props}
    >
      {children}
    </span>
  );
}

export function Carte({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn("rounded-lg border border-line bg-surface shadow-card", className)} {...props} />;
}

export function Squelette({ className }: { className?: string }) {
  return <div aria-hidden className={cn("animate-shimmer rounded-md bg-paper-deep", className)} />;
}

export function SurTitre({ children }: { children: ReactNode }) {
  return (
    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-forest">{children}</p>
  );
}

/* --------------------------------- Messages --------------------------------- */

type TonAlerte = "erreur" | "info" | "succes" | "attention";

const ALERTES: Record<TonAlerte, { fond: string; icone: string; Icone: typeof Info }> = {
  erreur: { fond: "border-terracotta/30 bg-terracotta-soft", icone: "text-terracotta", Icone: TriangleAlert },
  attention: { fond: "border-gold/30 bg-gold-soft", icone: "text-gold", Icone: TriangleAlert },
  info: { fond: "border-forest-line bg-forest-soft", icone: "text-forest", Icone: Info },
  succes: { fond: "border-forest-line bg-forest-soft", icone: "text-forest", Icone: CircleCheck },
};

export function Alerte({
  ton = "info",
  titre,
  children,
  action,
  className,
}: {
  ton?: TonAlerte;
  titre: string;
  children?: ReactNode;
  action?: ReactNode;
  className?: string;
}) {
  const { fond, icone, Icone } = ALERTES[ton];
  return (
    <div
      role={ton === "erreur" ? "alert" : undefined}
      className={cn("flex flex-col gap-4 rounded-lg border p-4 sm:flex-row sm:items-center sm:p-5", fond, className)}
    >
      <div className="flex min-w-0 flex-1 gap-3">
        <Icone className={cn("mt-0.5 size-5 shrink-0", icone)} aria-hidden />
        <div className="min-w-0">
          <p className="font-display text-base font-semibold text-ink">{titre}</p>
          {children && <div className="mt-1 text-sm leading-relaxed text-ink-muted">{children}</div>}
        </div>
      </div>
      {action}
    </div>
  );
}

export function EtatVide({
  illustration,
  titre,
  children,
  action,
}: {
  illustration: ReactNode;
  titre: string;
  children?: ReactNode;
  action?: ReactNode;
}) {
  return (
    <div className="flex flex-col items-center rounded-xl border border-dashed border-line-strong bg-surface/60 px-6 py-14 text-center">
      <div className="grid size-20 place-items-center rounded-full bg-forest-soft text-forest" aria-hidden>
        {illustration}
      </div>
      <h2 className="mt-6 font-display text-2xl font-semibold text-ink">{titre}</h2>
      {children && <p className="mt-2 max-w-md text-[0.9375rem] leading-relaxed text-ink-muted">{children}</p>}
      {action && <div className="mt-7">{action}</div>}
    </div>
  );
}
