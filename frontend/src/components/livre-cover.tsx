import { cn } from "@/lib/utils";

interface Palette {
  fond: string;
  texte: string;
  accent: string;
}

/* Couvertures typographiques : une palette par livre, choisie de facon stable a partir du titre. */
const PALETTES: Palette[] = [
  { fond: "linear-gradient(150deg,#27634c 0%,#143a2c 100%)", texte: "#f5eedb", accent: "#d9b86f" },
  { fond: "linear-gradient(150deg,#2f4060 0%,#161f33 100%)", texte: "#f1ebdb", accent: "#cba35e" },
  { fond: "linear-gradient(150deg,#c65a31 0%,#8f3520 100%)", texte: "#fbefe4", accent: "#f3d29c" },
  { fond: "linear-gradient(150deg,#caa041 0%,#8a6314 100%)", texte: "#fff7e1", accent: "#3f2c0a" },
  { fond: "linear-gradient(150deg,#6e3d5a 0%,#3d1f33 100%)", texte: "#f7e9ef", accent: "#e6bacb" },
  { fond: "linear-gradient(150deg,#7f9e89 0%,#4b6a58 100%)", texte: "#f7f4e9", accent: "#1f3a2c" },
];

function indexStable(texte: string): number {
  let somme = 0;
  for (const caractere of texte) somme = (somme * 31 + caractere.charCodeAt(0)) % 9973;
  return somme;
}

function Motif({ variante, couleur }: { variante: number; couleur: string }) {
  const commun = { fill: "none", stroke: couleur, strokeWidth: 0.9, opacity: 0.55 } as const;
  switch (variante % 6) {
    case 0:
      return (
        <g {...commun}>
          <circle cx="86" cy="118" r="18" />
          <circle cx="86" cy="118" r="30" />
          <circle cx="86" cy="118" r="42" />
        </g>
      );
    case 1:
      return (
        <g {...commun}>
          <circle cx="70" cy="86" r="14" fill={couleur} opacity={0.35} stroke="none" />
          <path d="M8 100 H92 M8 108 H92 M8 116 H92" />
        </g>
      );
    case 2:
      return (
        <g {...commun}>
          <path d="M-10 110 L60 40 M6 126 L76 56 M22 142 L92 72 M38 158 L108 88" />
        </g>
      );
    case 3:
      return (
        <g fill={couleur} opacity={0.45}>
          {[0, 1, 2, 3].flatMap((x) => [0, 1, 2].map((y) => <circle key={`${x}-${y}`} cx={58 + x * 11} cy={92 + y * 11} r={1.6} />))}
        </g>
      );
    case 4:
      return (
        <g {...commun}>
          <circle cx="50" cy="96" r="30" />
          <path d="M20 96 H80 M50 66 V126" />
        </g>
      );
    default:
      return (
        <g {...commun}>
          <path d="M14 126 L44 82 L74 126 Z M44 126 L68 94 L92 126 Z" />
        </g>
      );
  }
}

export function LivreCover({
  titre,
  auteur,
  className,
  compact = false,
}: {
  titre: string;
  auteur: string;
  className?: string;
  compact?: boolean;
}) {
  const graine = indexStable(titre + auteur);
  const palette = PALETTES[graine % PALETTES.length];
  const tailleTitre = compact
    ? "text-[0.8125rem] leading-tight"
    : titre.length > 42
      ? "text-lg leading-snug"
      : titre.length > 24
        ? "text-xl leading-snug"
        : "text-2xl leading-tight";

  return (
    <div
      role="img"
      aria-label={`Couverture de ${titre}, de ${auteur}`}
      className={cn("relative aspect-[3/4] overflow-hidden rounded-md shadow-card", className)}
      style={{ background: palette.fond, color: palette.texte }}
    >
      <svg viewBox="0 0 100 133" className="absolute inset-0 size-full" aria-hidden preserveAspectRatio="xMidYMid slice">
        <Motif variante={graine >> 3} couleur={palette.accent} />
      </svg>
      <div className="absolute inset-y-0 left-0 w-[7%] border-r border-black/15 bg-black/15" aria-hidden />
      <div className="absolute inset-0 flex flex-col justify-between py-[9%] pl-[16%] pr-[9%]">
        <span className="text-[0.55rem] font-semibold uppercase tracking-[0.28em] opacity-70">
          {compact ? "" : "SunuBiblio"}
        </span>
        <div>
          <p className={cn("font-display font-semibold", tailleTitre)}>{titre}</p>
          {!compact && (
            <p className="mt-3 text-[0.625rem] font-semibold uppercase tracking-[0.2em]" style={{ color: palette.accent }}>
              {auteur}
            </p>
          )}
        </div>
      </div>
    </div>
  );
}
