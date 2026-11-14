"use client";

import { TriangleAlert } from "lucide-react";
import Link from "next/link";
import { useEffect } from "react";
import { Bouton, EtatVide, classesBouton } from "@/components/ui";

export default function ErreurPage({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  useEffect(() => {
    console.error(error);
  }, [error]);

  return (
    <EtatVide
      illustration={<TriangleAlert className="size-9" />}
      titre="Quelque chose s'est mal passé"
      action={
        <div className="flex flex-col gap-3 sm:flex-row">
          <Bouton onClick={reset}>Réessayer</Bouton>
          <Link href="/" className={classesBouton("secondaire")}>
            Retourner au catalogue
          </Link>
        </div>
      }
    >
      Une erreur inattendue a interrompu l&apos;affichage de cette page. Vos données n&apos;ont pas été modifiées : réessayez, et si le
      problème persiste, rechargez la page.
      {error.digest && (
        <>
          {" "}
          Identifiant de suivi : <span className="font-mono text-ink">{error.digest}</span>.
        </>
      )}
    </EtatVide>
  );
}
