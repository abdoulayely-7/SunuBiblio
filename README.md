# SunuBiblio — prêts de la bibliothèque universitaire

Monolithe modulaire Spring Boot + Spring Modulith (TP Module 5.2, projet 2).
Base de données : PostgreSQL `SunuBiblio` (tests : H2 en mémoire).

Binôme : Mansour Niang et Abdoulaye Ly. Dépôt : `abdoulayely-7/SunuBiblio`, branche `feat/sunubiblio`.

Les neuf fonctionnalités F1 à F9 du sujet sont réalisées.

## 1. Conception

### 1.1 Les modules

| Module | Responsabilité | Il possède | Il expose (porte d'entrée) | Il annonce | Dépend de |
|---|---|---|---|---|---|
| `catalogue` | Connaître les livres et savoir si chaque exemplaire est disponible, emprunté ou retiré. | Livre, Exemplaire (états `DISPONIBLE`, `EMPRUNTE`, `RETIRE`) | `CatalogueApi`, `LivreDto`, `ExemplaireDto` | — | — |
| `adherents` | Inscrire les adhérents et dire combien d'exemplaires chacun peut emprunter. | Adhérent (type étudiant ou enseignant, quota) | `AdherentsApi`, `AdherentDto`, `TypeAdherent` | — | — |
| `penalites` | Calculer, enregistrer et encaisser les pénalités de retard. | Pénalité (jours de retard, montant, statut) | `PenalitesApi`, `PenaliteDto` | `PenaliteCreee`, `PenalitePayee` | — |
| `emprunts` (le prêt) | Prêter et reprendre les exemplaires en appliquant les règles de prêt (quota, 14 jours, blocage). Tout passe par lui. | Emprunt (dates de prêt, d'échéance et de retour), liste des adhérents bloqués | `EmpruntsApi`, `EmpruntDto`, `RetourDto`, `SituationAdherentDto` | — (personne ne dépend de lui) | `adherents`, `catalogue`, `penalites` |

Chaque module est un package sous `lydevtech.sunubiblio`. La porte d'entrée (interface, DTO, événements, exceptions publiques) est à la racine du module ; tout le reste est dans `internal` (`domain`, `application`, `infrastructure`).

### 1.2 Chaque fonctionnalité dans un seul module

| N° | Fonctionnalité | Module |
|---|---|---|
| F1 | Ajouter un livre et ses exemplaires | `catalogue` |
| F2 | Retirer un exemplaire abîmé | `catalogue` |
| F3 | S'inscrire comme adhérent (étudiant ou enseignant) | `adherents` |
| F4 | Rechercher un livre et voir les exemplaires disponibles | `catalogue` |
| F5 | Emprunter un exemplaire | `emprunts` |
| F6 | Rendre un exemplaire | `emprunts` |
| F7 | Créer une pénalité quand un exemplaire est rendu en retard | `penalites` |
| F8 | Payer sa pénalité | `penalites` |
| F9 | Consulter ses emprunts en cours et ses pénalités | `emprunts` (il lit déjà `penalites`) |

### 1.3 Règles du métier : où elles vivent

| Règle | Module | Où dans le code |
|---|---|---|
| Un étudiant emprunte au plus 3 exemplaires, un enseignant 5 | `adherents` (le quota) et `emprunts` (le comptage) | `TypeAdherent`, `RegleEmprunt` |
| Un prêt dure 14 jours | `emprunts` | `Emprunt` |
| La pénalité est de 100 XOF par jour de retard | `penalites` | `Penalite` |
| Un adhérent qui a une pénalité impayée ne peut pas emprunter | `emprunts` (la décision) grâce aux événements de `penalites` (l'information) | `RegleEmprunt`, `EcouteurPenalites` |

### 1.4 Schéma des dépendances (sans cycle, tout passe par le prêt)

```mermaid
flowchart LR
    emprunts -->|appel direct| adherents
    emprunts -->|appel direct| catalogue
    emprunts -->|appel direct| penalites
    emprunts -.->|écoute les événements| penalites
```

Trait plein : appel direct par l'API publique. Trait pointillé : écoute d'un événement (elle crée aussi une dépendance, vers le module qui définit l'événement). Seul `emprunts` a des flèches qui partent de lui : le sens est unique.

| Flèche | Type | Pourquoi |
|---|---|---|
| `emprunts` → `adherents` | appel direct | Il faut connaître le type et le quota de l'adhérent tout de suite pour accepter ou refuser l'emprunt. |
| `emprunts` → `catalogue` | appel direct | Il faut vérifier et marquer l'exemplaire « emprunté » dans la même transaction que l'emprunt ; au retour, il est rendu disponible de la même façon. |
| `emprunts` → `penalites` | appel direct | La pénalité est créée dans la même transaction que le retour en retard, et F9 lit les pénalités de l'adhérent. |
| `emprunts` → `penalites` | événements `PenaliteCreee`, `PenalitePayee` | Le prêt garde sa propre liste des adhérents bloqués (table `emprunts_blocage`) : il n'a pas à interroger `penalites` à chaque emprunt. |

### 1.5 Pourquoi il n'y a pas de cycle

Le piège du sujet est la paire `emprunts` / `penalites` : une pénalité naît d'un retour (le prêt parle à `penalites`), et le prêt doit savoir si une pénalité est impayée (information qui vient de `penalites`). Si `penalites` écoutait un événement du prêt, on aurait un cycle. J'ai choisi de faire passer tout par le prêt : `emprunts` appelle `penalites` et écoute ses événements, `penalites` ne connaît personne, et aucun module ne dépend d'`emprunts`. Comme l'événement est défini par `penalites`, écouter cet événement est une dépendance d'`emprunts` vers `penalites`, dans le même sens que les appels directs.

### 1.6 Les deux modes de communication

- Appels directs : `emprunts` appelle `AdherentsApi`, `CatalogueApi` et `PenalitesApi`.
- Événements : `penalites` publie `PenaliteCreee` et `PenalitePayee` ; `emprunts` les écoute (`EcouteurPenalites`) pour bloquer ou débloquer l'adhérent.
- Références par identifiant : seuls des `UUID` (identifiants d'adhérent, d'exemplaire, d'emprunt, de pénalité) et des DTO traversent les frontières, jamais un objet du domaine.

## 2. Structure du code

```text
lydevtech.sunubiblio
├── SunuBiblioApplication, DotenvEnvironmentPostProcessor, OpenApiConfiguration   (racine, hors module)
└── catalogue | adherents | penalites | emprunts
    ├── <Module>Api, DTO, événements, exceptions publiques     (porte d'entrée, racine du module)
    ├── package-info.java                                      (allowedDependencies)
    └── internal
        ├── domain          règles du métier (sans Spring) et ports (interfaces de dépôt)
        ├── application     services (cas d'usage) et écouteurs d'événements
        └── infrastructure  persistence (JDBC), web (contrôleurs), configuration
```

Chaque module possède ses tables (`catalogue_*`, `adherents_*`, `emprunts_*`, `penalites_*`) dans son propre script `src/main/resources/db/schema-<module>.sql`. Il n'y a aucune clé étrangère ni jointure entre modules.

## 3. Lancer le projet

Prérequis : JDK 17 ou plus, PostgreSQL avec une base nommée `SunuBiblio`.

```bash
cp .env.example .env        # puis renseigner DB_URL, DB_USERNAME et DB_PASSWORD
./mvnw spring-boot:run
```

Sous Windows PowerShell, depuis le dossier du projet :

```powershell
Copy-Item .env.example .env   # puis renseigner DB_URL, DB_USERNAME et DB_PASSWORD
.\mvnw.cmd spring-boot:run
```

Les autres commandes `./mvnw ...` de ce document s'écrivent `.\mvnw.cmd ...` sous PowerShell.

Le fichier `.env` n'est jamais commité. Les tables sont créées au démarrage.

### L'API REST

Swagger UI : <http://localhost:8080/swagger-ui.html> (pour essayer toutes les requêtes dans le navigateur).
Contrat OpenAPI : [`docs/openapi.yaml`](docs/openapi.yaml), identique à celui publié par l'application sur `/v3/api-docs` (un test le vérifie).

| Fonctionnalité | Requête |
|---|---|
| F1 Ajouter un livre et ses exemplaires | `POST /livres` `{"titre", "auteur", "nombreExemplaires"}` ; `POST /livres/{id}/exemplaires` |
| F2 Retirer un exemplaire abîmé | `DELETE /exemplaires/{id}` |
| F3 S'inscrire | `POST /adherents` `{"nom", "type": "ETUDIANT" ou "ENSEIGNANT"}` |
| F4 Rechercher un livre | `GET /livres?q=texte` ; `GET /livres/{id}` |
| F5 Emprunter | `POST /emprunts` `{"adherentId", "exemplaireId"}` |
| F6 Rendre | `POST /emprunts/{id}/retour` (corps optionnel `{"dateRetour": "AAAA-MM-JJ"}`, par défaut aujourd'hui) |
| F7 Pénalité de retard | automatique au retour en retard |
| F8 Payer une pénalité | `POST /penalites/{id}/paiement` |
| F9 Emprunts en cours et pénalités | `GET /situations/{adherentId}` |

Les erreurs suivent le format Problem Details (`application/problem+json`) : 400 requête invalide, 404 ressource inconnue, 409 règle du métier refusée.

Pour regénérer le contrat après un changement de l'API : `./mvnw test -Dtest=ContratOpenApiTest -Dopenapi.regenerer=true`.

### Rejouer le scénario à la main

```bash
curl -X POST localhost:8080/livres -H "Content-Type: application/json" \
  -d '{"titre":"Une si longue lettre","auteur":"Mariama Ba","nombreExemplaires":1}'
curl -X POST localhost:8080/adherents -H "Content-Type: application/json" \
  -d '{"nom":"Awa Diop","type":"ETUDIANT"}'
curl -X POST localhost:8080/emprunts -H "Content-Type: application/json" \
  -d '{"adherentId":"<id adhérent>","exemplaireId":"<id exemplaire>"}'
curl localhost:8080/livres/<id livre>                     # 0 exemplaire disponible
curl -X POST localhost:8080/emprunts/<id emprunt>/retour -H "Content-Type: application/json" \
  -d '{"dateRetour":"<dateEcheance + 3 jours>"}'          # pénalité de 300 XOF
curl -X POST localhost:8080/emprunts -H "Content-Type: application/json" \
  -d '{"adherentId":"<id adhérent>","exemplaireId":"<id exemplaire>"}'   # 409 : refusé
curl -X POST localhost:8080/penalites/<id pénalité>/paiement
curl -X POST localhost:8080/emprunts -H "Content-Type: application/json" \
  -d '{"adherentId":"<id adhérent>","exemplaireId":"<id exemplaire>"}'   # 201 : accepté
```

## 4. Tests

```bash
./mvnw test                      # toute la suite : 66 tests
./mvnw test -Dtest=ModulesTest   # seulement le gardien des frontières
```

Résultat observé : `Tests run: 66, Failures: 0, Errors: 0, Skipped: 0`.

| Test | Rôle |
|---|---|
| `ModulesTest` (2) | Le gardien : `verify()` doit passer, et les 4 modules attendus sont bien détectés (le test ne passe donc pas à vide). |
| `ScenarioBoutEnBoutTest` (1) | Le scénario du sujet, étape par étape par l'API REST, avec chaque résultat annoncé vérifié (exemplaire indisponible, retard de 3 jours, pénalité de 300 XOF, nouvel emprunt refusé, paiement, emprunt accepté). |
| Tests de domaine (26) | Les règles du métier sans Spring ni base : états d'un exemplaire, quotas, 14 jours, 100 XOF par jour, blocage. |
| Tests d'intégration (30) | Chaque module par l'API REST : catalogue, adhérents, pénalités (événements publiés), règles d'emprunt, blocage par événements (dont une pénalité annulée avec sa transaction qui ne bloque personne). |
| `ContratOpenApiTest` (5) | Tous les endpoints sont décrits, les erreurs suivent Problem Details, les codes de succès sont les vrais, Swagger UI est servi, et `docs/openapi.yaml` est identique au contrat de l'application. |
| `BaseDeTestTest` | Garde : les tests tournent sur H2 en mémoire, jamais sur la base PostgreSQL `SunuBiblio`. |

## 5. Étape 3 : casser une frontière

### 5.1 Accès à une classe `internal` d'un autre module

J'ai ajouté dans `emprunts` une classe qui utilise `lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire`. L'application compile (`./mvnw compile` réussit), mais `./mvnw test -Dtest=ModulesTest` échoue :

```text
org.springframework.modulith.core.Violations:
- Module 'emprunts' depends on non-exposed type lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire within module 'catalogue'!
Exemplaire declares return type Exemplaire.exemplaireInterneDuCatalogue() in (ViolationVolontaire.java:0)
- Module 'emprunts' depends on non-exposed type lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire within module 'catalogue'!
Method <lydevtech.sunubiblio.emprunts.internal.application.ViolationVolontaire.exemplaireInterneDuCatalogue()> has return type <lydevtech.sunubiblio.catalogue.internal.domain.Exemplaire> in (ViolationVolontaire.java:0)
```

Correction : j'ai supprimé la classe fautive. La bonne façon de faire est de passer par la porte d'entrée du module : `emprunts` appelle `CatalogueApi` (par exemple `marquerEmprunte(exemplaireId)`) et ne manipule que des identifiants et des DTO. Le domaine d'un module reste privé.

### 5.2 Cycle entre modules

J'ai ensuite fait dépendre `penalites` d'`emprunts` (elle lisait un `EmpruntDto`), alors qu'`emprunts` dépend déjà de `penalites`. L'application compile encore, mais `ModulesTest` échoue (message raccourci, `[...]` marque les lignes omitées) :

```text
org.springframework.modulith.core.Violations:
- Cycle detected: Slice emprunts ->
                Slice penalites ->
                Slice emprunts
  1. Dependencies of Slice emprunts
    [...]
    - Method <...EmpruntsService.rendre(java.util.UUID, java.time.LocalDate)> calls method <...PenalitesApi.creerPourRetard(java.util.UUID, java.util.UUID, int)> in (EmpruntsService.java:72)
    [...]
  2. Dependencies of Slice penalites
    - Method <...ViolationCycle.origine(lydevtech.sunubiblio.emprunts.EmpruntDto)> has parameter of type <lydevtech.sunubiblio.emprunts.EmpruntDto> in (ViolationCycle.java:0)
    - Method <...ViolationCycle.origine(...)> calls method <lydevtech.sunubiblio.emprunts.EmpruntDto.id()> in (ViolationCycle.java:9)
- Module 'penalites' depends on module 'emprunts' via lydevtech.sunubiblio.penalites.internal.application.ViolationCycle -> lydevtech.sunubiblio.emprunts.EmpruntDto. Allowed targets: none.
```

Correction : j'ai supprimé la classe fautive. Tout passe par le prêt, dans un seul sens : `penalites` reçoit ce dont elle a besoin en paramètres (`creerPourRetard(adherentId, empruntId, joursDeRetard)`) et ne demande rien à `emprunts` ; si elle doit prévenir quelqu'un, elle annonce un événement que le prêt écoute.

Dans les deux cas la suite complète est repassée au vert (66 tests).

## 6. Décisions d'architecture

| Décision | Contexte | Conséquences |
|---|---|---|
| Le prêt est l'unique point de passage : seul `emprunts` dépend des autres | Un sens unique évite tout cycle, et le prêt est le cœur du métier. | Aucun module ne dépend d'`emprunts`, qui n'annonce donc aucun événement. Ajouter un module qui réagit à un emprunt demanderait de revoir ce choix. |
| Le blocage d'un adhérent vient des événements `PenaliteCreee` et `PenalitePayee` | Le prêt doit refuser un emprunt si une pénalité est impayée, sans que `penalites` ne dépende du prêt. | Le prêt garde sa propre table `emprunts_blocage` (clé : identifiant de la pénalité, donc idempotente). L'écoute est synchrone et dans la transaction de l'émetteur : le blocage est enregistré ou annulé avec la pénalité (testé). |
| Pas de module de notifications | Les notifications ne font pas partie des fonctionnalités F1 à F9 ; les événements servent le métier. | Le module a été retiré ; les événements ont un vrai rôle (le blocage). |
| PostgreSQL à l'exécution, H2 en mémoire pour les tests | Base demandée : `SunuBiblio`. Les tests ne doivent jamais modifier les vraies données. | Le profil `test` impose H2 ; `BaseDeTestTest` le vérifie. Le `.env` a la priorité la plus basse et ne peut pas écraser une propriété de test. |
| Chaque règle à un seul endroit | Les valeurs (3 et 5 exemplaires, 14 jours, 100 XOF) ne doivent pas être recopiées. | `TypeAdherent` (quotas), `Emprunt.DUREE_PRET_JOURS`, `Penalite.TARIF_PAR_JOUR_XOF`. |
| Changements d'état par mise à jour conditionnelle (`... WHERE etat = ?`) | Deux emprunts simultanés du même exemplaire. | Un seul gagne ; l'autre reçoit un 409. |
| Contrat OpenAPI généré par springdoc, copié dans `docs/openapi.yaml` et verrouillé par un test | Le contrat doit rester cohérent avec le comportement réel. | Si l'API change sans mise à jour du fichier, `ContratOpenApiTest` échoue. Les erreurs sont décrites une seule fois (`ProblemDetail`). |

### Hypothèses retenues (règles ambiguës)

1. Un retour le jour de l'échéance, ou avant, n'est pas en retard ; le retard se compte en jours calendaires après l'échéance.
2. Un adhérent avec une pénalité impayée ne peut plus emprunter, mais peut toujours rendre ses exemplaires. Avec plusieurs pénalités impayées, il faut les payer toutes.
3. Le quota compte les emprunts en cours (non rendus).
4. Un exemplaire en cours d'emprunt ne peut pas être retiré (il doit d'abord être rendu) ; un exemplaire retiré ne peut plus être emprunté.
5. Une pénalité est créée par emprunt rendu en retard et se paie en une seule fois.
6. Le retour accepte une date (par défaut aujourd'hui, jamais avant la date d'emprunt) pour rejouer un retard sans attendre 14 jours.
7. Pas d'authentification (hors périmètre du TP).

### Glossaire

| Terme | Sens | À éviter |
|---|---|---|
| Livre | Une œuvre du catalogue (titre, auteur). | document, ouvrage |
| Exemplaire | Un exemplaire physique d'un livre, `DISPONIBLE`, `EMPRUNTE` ou `RETIRE`. | copie, volume |
| Adhérent | Étudiant ou enseignant inscrit à la bibliothèque. | usager, lecteur, client |
| Emprunt | Le prêt d'un exemplaire à un adhérent, pour 14 jours. | prêt (en dehors de la durée), location |
| Retour | Le moment où l'adhérent rend l'exemplaire. | restitution |
| Pénalité | Somme due pour un retard (100 XOF par jour), `IMPAYEE` ou `PAYEE`. | amende, frais de retard |
| Blocage | État d'un adhérent qui a au moins une pénalité impayée : il ne peut plus emprunter. | suspension, interdiction |
