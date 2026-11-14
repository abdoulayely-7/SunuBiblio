import { Compass } from "lucide-react";
import Link from "next/link";
import { EtatVide, classesBouton } from "@/components/ui";

export default function IntrouvablePage() {
  return (
    <EtatVide
      illustration={<Compass className="size-9" />}
      titre="Cette page n'existe pas"
      action={
        <Link href="/" className={classesBouton("primaire")}>
          Retourner au catalogue
        </Link>
      }
    >
      L&apos;adresse saisie ne correspond à aucune page de SunuBiblio. Vérifiez-la, ou repartez du catalogue pour retrouver votre livre.
    </EtatVide>
  );
}
