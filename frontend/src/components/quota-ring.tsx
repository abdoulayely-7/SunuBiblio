import { cn } from "@/lib/utils";

/** Jauge circulaire : combien d'exemplaires l'adherent emprunte sur son maximum. */
export function QuotaRing({ utilises, quota }: { utilises: number; quota: number }) {
  const rayon = 52;
  const circonference = 2 * Math.PI * rayon;
  const ratio = quota > 0 ? Math.min(utilises / quota, 1) : 0;
  const plein = utilises >= quota;

  return (
    <div
      className="relative size-36 shrink-0"
      role="img"
      aria-label={`${utilises} exemplaire${utilises > 1 ? "s" : ""} emprunté${utilises > 1 ? "s" : ""} sur ${quota} possibles`}
    >
      <svg viewBox="0 0 128 128" className="size-full -rotate-90">
        <circle cx="64" cy="64" r={rayon} fill="none" strokeWidth="11" className="stroke-paper-deep" />
        <circle
          cx="64"
          cy="64"
          r={rayon}
          fill="none"
          strokeWidth="11"
          strokeLinecap="round"
          strokeDasharray={circonference}
          strokeDashoffset={circonference * (1 - ratio)}
          className={cn("transition-[stroke-dashoffset] duration-700 ease-soft", plein ? "stroke-terracotta" : "stroke-forest")}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">
        <span className="font-display text-4xl font-semibold leading-none text-ink">
          {utilises}
          <span className="text-xl text-ink-faint">/{quota}</span>
        </span>
        <span className="mt-1 text-xs font-medium text-ink-muted">emprunts</span>
      </div>
    </div>
  );
}
