# Spoony Backend

API REST pour l'application **Spoony**, une application de gestion de taches basee sur la **Theorie des Cuilleres** (*Spoon Theory*). Concue pour aider les personnes atteintes de maladies chroniques ou de handicaps a gerer leur energie quotidienne de maniere bienveillante.

> ⚠️ **Important — branche de travail**
> Le developpement actif se fait sur la branche **`dev`**. La branche `main` n'est pas a jour pour le moment.
> **Travaillez et deployez depuis `dev`** :
> ```bash
> git checkout dev
> ```

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Langage | Java 21 |
| Framework | Spring Boot 3.5.11 |
| Base de donnees | PostgreSQL 16 |
| Migrations | Flyway |
| Authentification | JWT (access + refresh tokens) |
| Documentation API | SpringDoc OpenAPI (Swagger) |
| Build | Maven |
| Conteneurisation | Docker + Docker Compose |
| Tests | JUnit 5 + Mockito + Spring Boot Test |

## Prerequis

- **Java 21+** (Eclipse Temurin recommande)
- **Maven 3+** (ou utiliser le wrapper `./mvnw` inclus)
- **Docker & Docker Compose** (pour PostgreSQL)

## Installation

```bash
# 1. Cloner le depot
git clone git@github.com:MartinChrrr/Spoony-back.git
cd spoony-backend

# 2. Se placer sur la branche de developpement (IMPORTANT)
git checkout dev

# 3. Lancer PostgreSQL et Adminer via Docker
# (uniquement la BDD et Adminer — le service backend du compose est demarre
#  separement, voir la section Docker)
docker-compose up -d db adminer

# 4. Lancer l'application avec le profil dev
# Le profil doit etre passe explicitement : aucun profil n'est actif par defaut,
# et sans profil l'application ne demarre pas (pas de datasource configuree).
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

L'API est accessible sur `http://localhost:8080`.
Adminer (interface BDD) est accessible sur `http://localhost:8081`.

> Note : attendez que le conteneur PostgreSQL soit pret (healthcheck) avant de lancer
> l'application. Le schema est cree exclusivement par les migrations **Flyway**
> (`ddl-auto: validate`) : Hibernate ne cree jamais les tables lui-meme.

## Configuration

Le projet utilise 3 profils Spring :

| Profil | Usage | Source de configuration |
|--------|-------|-------------------------|
| `dev` | Developpement local | Valeurs codees en dur dans `application-dev.yml` (BDD locale, secret JWT de dev) |
| `docker` | Stack Docker Compose complete | Variables d'environnement (injectees par `docker-compose.yml`) |
| `prod` | Production | Variables d'environnement (obligatoires, pas de valeurs par defaut sensibles) |

Variables d'environnement (lues par les profils `docker` et `prod` — le profil `dev` n'en a pas besoin) :

| Variable | Description | Profils | Defaut |
|----------|-------------|---------|--------|
| `DATABASE_URL` | URL JDBC PostgreSQL | docker, prod | — |
| `DATABASE_USER` | Utilisateur BDD | docker, prod | — |
| `DATABASE_PASSWORD` | Mot de passe BDD | docker, prod | — |
| `JWT_SECRET` | Cle secrete pour signer les tokens JWT (min. 256 bits) | docker, prod | — |
| `JWT_ACCESS_EXPIRATION` | Duree de vie du token d'acces (ms) | docker, prod | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION` | Duree de vie du refresh token (ms) | prod uniquement | `604800000` (7 jours) |
| `CORS_ALLOWED_ORIGINS` | Origines CORS autorisees (liste separee par des virgules) | prod (obligatoire) | `http://localhost:3000` (defaut global) |
| `SPRING_PROFILES_ACTIVE` | Profil Spring actif | tous | aucun (a definir explicitement) |

> Note : un fichier `.env` peut servir de reference pour vos deploiements, mais il
> n'est **pas versionne** (gitignore) et n'est **pas charge automatiquement** par
> `./mvnw spring-boot:run` (pas de dependance dotenv). En dev, tout est deja
> configure dans `application-dev.yml`.

## Architecture

Le projet suit une **architecture hexagonale** (Ports & Adapters) :

```
src/main/java/com/spoony/backend/
|
|-- application/              # Couche Application (adaptateurs entrants)
|   |-- auth/                 # Authentification (register, login, refresh)
|   |-- energy/               # Service applicatif Energy (orchestration)
|   |-- tasklog/              # Service applicatif TaskLog (orchestration)
|   +-- rest/                 # Controleurs REST + DTOs
|       |-- task/
|       |-- energy/
|       |-- tasklog/
|       |-- suggestion/
|       |-- basetask/
|       |-- message/
|       |-- user/
|       +-- common/           # Reponse JSend, gestion d'erreurs globale
|
|-- domain/                   # Couche Domaine (logique metier pure)
|   |-- task/
|   |   |-- model/            # Modeles de domaine (POJOs)
|   |   |-- service/          # Implementations des use cases
|   |   |-- port/in/          # Use cases (interfaces entrantes)
|   |   +-- port/out/         # Contrats de persistance (interfaces sortantes)
|   |-- energy/               # (meme structure)
|   |-- tasklog/              # (meme structure)
|   |-- suggestion/           # (meme structure + strategy/ : strategies de scoring)
|   +-- shared/
|       |-- exception/        # Exceptions metier
|       +-- port/             # Ports partages entre domaines
|
+-- infrastructure/           # Couche Infrastructure (adaptateurs sortants)
    |-- persistence/
    |   |-- adapter/          # Implementations des ports de persistance
    |   |-- entity/           # Entites JPA
    |   |-- repository/       # Repositories Spring Data JPA
    |   +-- mapper/           # Mappers entite <-> domaine
    |-- security/             # Filtre JWT, provider de tokens, rate limiting
    +-- config/               # Configuration Spring (Security, CORS, OpenAPI, scheduler)
```

### Domaines metier

| Domaine | Description |
|---------|-------------|
| **Task** | Gestion des taches utilisateur (CRUD, cout en cuilleres, importance) |
| **Energy** | Declaration et suivi de l'energie quotidienne (0-12 cuilleres) |
| **TaskLog** | Journal d'execution des taches (planification, completion, report) |
| **Suggestion** | Moteur de suggestions intelligentes base sur l'energie disponible |

> User, BaseTask et Message sont des features plus legeres gerees directement dans
> les couches application/persistence (pas de package domaine complet).

## Endpoints API

Toutes les reponses suivent le format **JSend** :

```json
{
  "status": "success",
  "data": { ... }
}
```

### Authentification

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `POST` | `/api/auth/register` | Inscription (email, password, firstName) | Non |
| `POST` | `/api/auth/login` | Connexion, retourne les tokens JWT | Non |
| `POST` | `/api/auth/refresh` | Rafraichir le token d'acces | Non |

Les endpoints proteges necessitent le header : `Authorization: Bearer <token>`

> **Rate limiting** : les routes `/api/auth/**` sont limitees a **10 requetes/minute
> par IP**. Au-dela, l'API renvoie `429` avec le code `RATE_LIMITED`.

### Taches

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/tasks` | Lister les taches actives | Oui |
| `GET` | `/api/tasks/{id}` | Detail d'une tache | Oui |
| `POST` | `/api/tasks` | Creer une tache (seul `name` est requis) | Oui |
| `POST` | `/api/tasks/from-catalog` | Creer des taches depuis le catalogue de taches predefinies | Oui |
| `PUT` | `/api/tasks/{id}` | Modifier une tache | Oui |
| `DELETE` | `/api/tasks/{id}` | Supprimer une tache | Oui |

> Creation rapide : seul le champ `name` est obligatoire. Valeurs par defaut : `spoonCost=2`, `importance=MEDIUM`, `dueDate=aujourd'hui`.

### Energie / Cuilleres

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/energy/today` | Energie declaree aujourd'hui | Oui |
| `POST` | `/api/energy` | Declarer l'energie du jour (0-12 cuilleres) | Oui |
| `PUT` | `/api/energy/today` | Reevaluer les cuilleres en cours de journee | Oui |
| `PATCH` | `/api/energy/today/mood` | Enregistrer l'humeur de fin de journee | Oui |

> Si l'energie declaree est **0**, toutes les taches actives sont automatiquement reportees au lendemain.

### Journal des taches (Task Logs)

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/task-logs` | Logs du jour (`?include_archived=true` pour les archives, `?from=YYYY-MM-DD&to=YYYY-MM-DD` pour une plage / vue calendrier) | Oui |
| `POST` | `/api/task-logs` | Creer des logs en masse (statut PLANNED) | Oui |
| `POST` | `/api/task-logs/manual` | Creer un log manuel | Oui |
| `PATCH` | `/api/task-logs/{id}/status` | Changer le statut d'un log | Oui |
| `POST` | `/api/task-logs/bulk-postpone` | Reporter toutes les taches PLANNED | Oui |

### Suggestions

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/suggestions` | Obtenir les suggestions du jour (classees par score) | Oui |

### Taches predefinies (Catalogue)

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/base-tasks` | Lister le catalogue de taches predefinies (`?category=`, `?locale=`) | Oui |

### Messages bienveillants

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/messages/random` | Message aleatoire par contexte (`?context=`, `?locale=`) | Oui |

### Utilisateurs

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/users/me/export` | Exporter toutes mes donnees personnelles (RGPD, Art. 15 & 20) | Oui |
| `DELETE` | `/api/users/me` | Supprimer mon compte et toutes mes donnees (RGPD) | Oui |

## Base de donnees

### Tables principales

| Table | Description |
|-------|-------------|
| `users` | Comptes utilisateurs (email, mot de passe hashe, prenom) |
| `user_tasks` | Taches des utilisateurs (cout en cuilleres, importance, echeance) |
| `daily_energy` | Energie quotidienne declaree par utilisateur |
| `user_task_logs` | Journal d'execution des taches par jour |
| `base_tasks` | Catalogue de taches predefinies (7 categories) |
| `benevolent_messages` | Messages d'encouragement contextuels |

Les migrations sont gerees par **Flyway** dans `src/main/resources/db/migration/`.

### Retention des donnees (RGPD)

Un job planifie (`DataRetentionScheduler`) s'execute **tous les jours a 3h00** et
supprime definitivement les comptes inactifs depuis plus de **24 mois** (derniere
connexion ou, a defaut, date de creation).

## Tests

```bash
# Lancer tous les tests
./mvnw test

# Lancer les tests avec le rapport
./mvnw test -Dmaven.test.failure.ignore=false
```

Les tests utilisent une base **H2 en memoire**.

## Docker

### Developpement (base de donnees uniquement)

```bash
docker-compose up -d db adminer
```

Services lances :
- **PostgreSQL 16** sur le port `5432`
- **Adminer** sur le port `8081` (interface web pour la BDD)

### Stack complete (BDD + backend + Adminer)

```bash
docker-compose up -d
```

Lance en plus le **backend** (profil `docker`) sur le port `8080`.

> ⚠️ Ne lancez pas `./mvnw spring-boot:run` en parallele de la stack complete :
> le port `8080` serait deja occupe par le conteneur backend.

### Build de l'image de production

```bash
# Build multi-stage (JDK 21 -> JRE 21 Alpine)
docker build -t spoony-backend:latest .

# Lancer le conteneur avec le profil prod et un fichier d'env de production
# (DATABASE_URL vers la vraie BDD, JWT_SECRET fort, CORS_ALLOWED_ORIGINS, etc.)
docker run -p 8080:8080 --env-file .env.prod \
  -e SPRING_PROFILES_ACTIVE=prod spoony-backend
```

> Le profil `prod` exige `CORS_ALLOWED_ORIGINS` (pas de defaut) et desactive
> Swagger UI et la doc OpenAPI.

## Documentation API interactive

Une fois l'application lancee (profils `dev` ou `docker`), la documentation Swagger est accessible sur :

- **Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON** : `http://localhost:8080/api-docs`

> En profil `prod`, Swagger UI et la doc OpenAPI sont desactives.
