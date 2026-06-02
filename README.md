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
docker-compose up -d

# 4. Configurer les variables d'environnement
# Le fichier .env est deja configure pour le developpement local.
# Modifiez-le si necessaire.

# 5. Lancer l'application
./mvnw spring-boot:run
```

L'API est accessible sur `http://localhost:8080`.
Adminer (interface BDD) est accessible sur `http://localhost:8081`.

## Configuration

Variables d'environnement (fichier `.env`) :

| Variable | Description | Valeur par defaut (dev) |
|----------|-------------|-------------------------|
| `DATABASE_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://localhost:5432/spoony_dev` |
| `DATABASE_USER` | Utilisateur BDD | `spoony` |
| `DATABASE_PASSWORD` | Mot de passe BDD | `spoony_dev_password` |
| `JWT_SECRET` | Cle secrete pour signer les tokens JWT | cle de dev (a changer en prod) |
| `JWT_ACCESS_EXPIRATION` | Duree de vie du token d'acces (ms) | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION` | Duree de vie du refresh token (ms) | `604800000` (7 jours) |
| `SPRING_PROFILES_ACTIVE` | Profil Spring actif | `dev` |

## Architecture

Le projet suit une **architecture hexagonale** (Ports & Adapters) :

```
src/main/java/com/spoony/backend/
|
|-- application/              # Couche Application (adaptateurs entrants)
|   |-- rest/                 # Controleurs REST + DTOs
|   |   |-- task/
|   |   |-- energy/
|   |   |-- tasklog/
|   |   |-- suggestion/
|   |   |-- basetask/
|   |   |-- message/
|   |   |-- user/
|   |   +-- common/           # Reponse JSend, gestion d'erreurs globale
|   +-- auth/                 # Authentification (register, login, refresh)
|
|-- domain/                   # Couche Domaine (logique metier pure)
|   |-- task/
|   |   |-- model/            # Modeles de domaine (POJOs)
|   |   |-- ports/in/         # Use cases (interfaces entrantes)
|   |   +-- ports/out/        # Contrats de persistance (interfaces sortantes)
|   |-- energy/
|   |-- tasklog/
|   +-- suggestion/
|
+-- infrastructure/           # Couche Infrastructure (adaptateurs sortants)
    |-- persistence/
    |   |-- adapter/          # Implementations des ports de persistance
    |   |-- entity/           # Entites JPA
    |   |-- repository/       # Repositories Spring Data JPA
    |   +-- mapper/           # Mappers entite <-> domaine
    |-- config/               # Configuration Spring (Security, CORS, OpenAPI)
    +-- exception/            # Exceptions metier
```

### Domaines metier

| Domaine | Description |
|---------|-------------|
| **Task** | Gestion des taches utilisateur (CRUD, cout en cuilleres, importance) |
| **Energy** | Declaration et suivi de l'energie quotidienne (0-12 cuilleres) |
| **TaskLog** | Journal d'execution des taches (planification, completion, report) |
| **Suggestion** | Moteur de suggestions intelligentes base sur l'energie disponible |

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

### Taches

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/tasks` | Lister les taches actives | Oui |
| `GET` | `/api/tasks/{id}` | Detail d'une tache | Oui |
| `POST` | `/api/tasks` | Creer une tache (seul `name` est requis) | Oui |
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
| `GET` | `/api/task-logs` | Logs du jour (`?include_archived=true` pour inclure les archives) | Oui |
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
| `GET` | `/api/base-tasks` | Lister le catalogue de taches predefinies (`?category=`, `?locale=`) | Non |

### Messages bienveillants

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| `GET` | `/api/messages/random` | Message aleatoire par contexte (`?context=`, `?locale=`) | Non |

### Utilisateurs

| Methode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
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
docker-compose up -d
```

Services lances :
- **PostgreSQL 16** sur le port `5432`
- **Adminer** sur le port `8081` (interface web pour la BDD)

### Build de l'image de production

```bash
# Build multi-stage (JDK 21 -> JRE 21 Alpine)
docker build -t spoony-backend:latest .

# Lancer le conteneur
docker run -p 8080:8080 --env-file .env spoony-backend
```

## Documentation API interactive

Une fois l'application lancee, la documentation Swagger est accessible sur :

- **Swagger UI** : `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON** : `http://localhost:8080/v3/api-docs`
