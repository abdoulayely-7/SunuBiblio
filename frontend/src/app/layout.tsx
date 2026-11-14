import type { Metadata } from "next";
import { DM_Sans, Fraunces } from "next/font/google";
import "./globals.css";
import { Providers } from "@/components/providers";
import { BarreOnglets, SiteHeader } from "@/components/site-header";

const fraunces = Fraunces({
  subsets: ["latin"],
  variable: "--font-fraunces",
  axes: ["opsz"],
  display: "swap",
});

const dmSans = DM_Sans({
  subsets: ["latin"],
  variable: "--font-dm-sans",
  display: "swap",
});

export const metadata: Metadata = {
  title: {
    default: "SunuBiblio — la bibliothèque universitaire",
    template: "%s · SunuBiblio",
  },
  description:
    "Cherchez un livre, empruntez-le en un clic et suivez vos retours et vos pénalités dans la bibliothèque universitaire.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr" className={`${fraunces.variable} ${dmSans.variable}`}>
      <body className="flex min-h-dvh flex-col">
        <Providers>
          <a
            href="#contenu"
            className="sr-only z-50 rounded-md bg-forest px-4 py-2 text-surface focus:not-sr-only focus:fixed focus:left-4 focus:top-4"
          >
            Aller au contenu
          </a>
          <SiteHeader />
          <main id="contenu" className="mx-auto w-full max-w-7xl flex-1 px-4 pb-32 pt-10 sm:px-6 md:pb-20">
            {children}
          </main>
          <footer className="hidden border-t border-line py-8 text-center text-sm text-ink-faint md:block">
            SunuBiblio · bibliothèque universitaire · prêt de 14 jours, 100 XOF par jour de retard
          </footer>
          <BarreOnglets />
        </Providers>
      </body>
    </html>
  );
}
