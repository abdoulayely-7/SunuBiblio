"use client";

import {
  Check,
  ChevronDown,
  ClipboardList,
  LibraryBig,
  LogOut,
  UserPlus,
  UserRound,
  type LucideIcon,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useRef, useState } from "react";
import { initiales, libelleType } from "@/lib/format";
import { cn } from "@/lib/utils";
import { useAdherent } from "./providers";
import { Badge, Squelette, classesBouton } from "./ui";

interface Rubrique {
  href: string;
  libelle: string;
  icone: LucideIcon;
}

const RUBRIQUES: Rubrique[] = [
  { href: "/", libelle: "Catalogue", icone: LibraryBig },
  { href: "/mon-compte", libelle: "Mon compte", icone: UserRound },
  { href: "/bibliothecaire", libelle: "Bibliothécaire", icone: ClipboardList },
];

function estActive(chemin: string, href: string) {
  return href === "/" ? chemin === "/" : chemin.startsWith(href);
}

function MenuAdherent() {
  const { pret, adherent, connus, choisir, seDeconnecter } = useAdherent();
  const [ouvert, setOuvert] = useState(false);
  const conteneur = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!ouvert) return;
    function dehors(evenement: PointerEvent) {
      if (!conteneur.current?.contains(evenement.target as Node)) setOuvert(false);
    }
    function echap(evenement: KeyboardEvent) {
      if (evenement.key === "Escape") setOuvert(false);
    }
    document.addEventListener("pointerdown", dehors);
    document.addEventListener("keydown", echap);
    return () => {
      document.removeEventListener("pointerdown", dehors);
      document.removeEventListener("keydown", echap);
    };
  }, [ouvert]);

  if (!pret) return <Squelette className="h-11 w-40" />;

  if (!adherent) {
    return (
      <Link href="/inscription" className={classesBouton("primaire", true)}>
        <UserPlus className="size-4" aria-hidden />
        Rejoindre
      </Link>
    );
  }

  return (
    <div ref={conteneur} className="relative">
      <button
        type="button"
        onClick={() => setOuvert((v) => !v)}
        aria-haspopup="menu"
        aria-expanded={ouvert}
        className="flex h-11 items-center gap-2.5 rounded-full border border-line-strong bg-surface py-1 pl-1 pr-3 transition-colors hover:border-forest"
      >
        <span className="grid size-9 place-items-center rounded-full bg-forest text-sm font-semibold text-surface" aria-hidden>
          {initiales(adherent.nom)}
        </span>
        <span className="hidden max-w-36 truncate text-sm font-medium text-ink sm:block">{adherent.nom}</span>
        <ChevronDown className={cn("size-4 text-ink-faint transition-transform", ouvert && "rotate-180")} aria-hidden />
      </button>

      {ouvert && (
        <div
          role="menu"
          className="animate-pop absolute right-0 top-[calc(100%+0.5rem)] z-50 w-72 overflow-hidden rounded-lg border border-line bg-surface shadow-lift"
        >
          <p className="px-4 pb-1 pt-3 text-xs font-semibold uppercase tracking-[0.16em] text-ink-faint">Adhérent actif</p>
          <ul className="max-h-64 overflow-y-auto px-2 pb-2">
            {connus.map((a) => (
              <li key={a.id}>
                <button
                  type="button"
                  role="menuitemradio"
                  aria-checked={a.id === adherent.id}
                  onClick={() => {
                    choisir(a);
                    setOuvert(false);
                  }}
                  className="flex min-h-11 w-full items-center gap-3 rounded-md px-2 text-left transition-colors hover:bg-paper-deep"
                >
                  <span className="grid size-8 shrink-0 place-items-center rounded-full bg-forest-soft text-xs font-semibold text-forest-deep" aria-hidden>
                    {initiales(a.nom)}
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="block truncate text-sm font-medium text-ink">{a.nom}</span>
                    <span className="block text-xs text-ink-faint">{libelleType(a.type)}</span>
                  </span>
                  {a.id === adherent.id && <Check className="size-4 shrink-0 text-forest" aria-hidden />}
                </button>
              </li>
            ))}
          </ul>
          <div className="border-t border-line p-2">
            <Link
              href="/inscription"
              role="menuitem"
              onClick={() => setOuvert(false)}
              className="flex min-h-11 items-center gap-3 rounded-md px-2 text-sm text-ink-muted transition-colors hover:bg-paper-deep hover:text-ink"
            >
              <UserPlus className="size-4" aria-hidden />
              Inscrire un adhérent ou ouvrir un compte
            </Link>
            <button
              type="button"
              role="menuitem"
              onClick={() => {
                seDeconnecter();
                setOuvert(false);
              }}
              className="flex min-h-11 w-full items-center gap-3 rounded-md px-2 text-left text-sm text-terracotta-deep transition-colors hover:bg-terracotta-soft"
            >
              <LogOut className="size-4" aria-hidden />
              Se déconnecter
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export function SiteHeader() {
  const chemin = usePathname();
  const { adherent } = useAdherent();

  return (
    <header className="sticky top-0 z-40 border-b border-line/80 bg-paper/85 backdrop-blur-md">
      <div className="mx-auto flex h-[4.25rem] max-w-7xl items-center justify-between gap-4 px-4 sm:px-6">
        <div className="flex items-center gap-10">
          <Link href="/" className="flex items-center gap-3" aria-label="SunuBiblio, retour au catalogue">
            <span className="grid size-10 place-items-center rounded-md bg-forest text-surface shadow-[0_8px_16px_-8px_rgb(30_77_59/0.7)]" aria-hidden>
              <LibraryBig className="size-5" />
            </span>
            <span className="font-display text-[1.375rem] font-semibold tracking-tight text-ink">SunuBiblio</span>
          </Link>

          <nav aria-label="Navigation principale" className="hidden items-center gap-1 md:flex">
            {RUBRIQUES.map(({ href, libelle }) => (
              <Link
                key={href}
                href={href}
                aria-current={estActive(chemin, href) ? "page" : undefined}
                className={cn(
                  "relative flex h-11 items-center rounded-md px-4 text-[0.9375rem] font-medium transition-colors",
                  estActive(chemin, href) ? "text-forest-deep" : "text-ink-muted hover:text-ink",
                )}
              >
                {libelle}
                {estActive(chemin, href) && (
                  <span className="absolute inset-x-4 -bottom-[0.9rem] h-0.5 rounded-full bg-forest" aria-hidden />
                )}
              </Link>
            ))}
          </nav>
        </div>

        <div className="flex items-center gap-3">
          {adherent && (
            <Badge ton={adherent.type === "ENSEIGNANT" ? "gold" : "forest"} className="hidden lg:inline-flex">
              {libelleType(adherent.type)}
            </Badge>
          )}
          <MenuAdherent />
        </div>
      </div>
    </header>
  );
}

/** Barre d'onglets fixe pour les petits ecrans. */
export function BarreOnglets() {
  const chemin = usePathname();
  const onglets: Rubrique[] = [...RUBRIQUES, { href: "/inscription", libelle: "Rejoindre", icone: UserPlus }];

  return (
    <nav
      aria-label="Navigation principale"
      className="fixed inset-x-0 bottom-0 z-40 border-t border-line bg-surface/95 pb-[env(safe-area-inset-bottom)] backdrop-blur-md md:hidden"
    >
      <ul className="grid grid-cols-4">
        {onglets.map(({ href, libelle, icone: Icone }) => {
          const active = estActive(chemin, href);
          return (
            <li key={href}>
              <Link
                href={href}
                aria-current={active ? "page" : undefined}
                className={cn(
                  "relative flex min-h-16 flex-col items-center justify-center gap-1 text-[0.6875rem] font-medium transition-colors",
                  active ? "text-forest-deep" : "text-ink-faint",
                )}
              >
                {active && <span className="absolute inset-x-6 top-0 h-0.5 rounded-full bg-forest" aria-hidden />}
                <Icone className="size-5" aria-hidden />
                {libelle}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
