# Déploiement AWS V0 — guide pas-à-pas

Mise en ligne du backend Spoony sur **AWS ECS Fargate** en **eu-west-3 (Paris)**.
Ce fichier est la checklist actionnable ; voir [`README.md`](./README.md) pour
l'architecture détaillée, les coûts et la dette post-V0.

> ⚠️ Aucune ressource AWS n'est créée tant que **tu** ne lances pas `terraform
> apply` avec **ton** compte. Cela génère des coûts (~50-55 €/mois, cf. plus bas).

---

## 0. Prérequis (à installer une fois)

```bash
# Terraform >= 1.5
terraform -version

# AWS CLI v2 + identifiants (clé IAM avec droits admin ou équivalents)
aws --version
aws configure          # région: eu-west-3, format: json
aws sts get-caller-identity   # doit afficher ton compte

# Docker (pour la 1re image si tu ne passes pas par le pipeline)
docker --version

# jq (utilisé par le bootstrap manuel d'image)
jq --version
```

Optionnel (pour le HTTPS) : un **domaine** et un **certificat ACM dans
eu-west-3**. Sans certificat, l'ALB ne sert que du HTTP:80 (bootstrap seulement,
**pas pour de vrais utilisateurs / données de santé**).

---

## 1. Provisionner l'infrastructure

```bash
cd infra

terraform init                       # backend local par défaut

cp terraform.tfvars.example terraform.tfvars
# Éditer terraform.tfvars :
#   - cors_allowed_origins  (OBLIGATOIRE — origines de l'app, séparées par des virgules)
#   - acm_certificate_arn   (optionnel — ARN du certificat pour activer le HTTPS)
#   - laisser container_image sur le placeholder busybox pour ce 1er apply

terraform plan                       # relire ce qui va être créé
terraform apply                      # taper "yes"
```

Crée : VPC (sans NAT), RDS PostgreSQL chiffrée, ECR, Secrets Manager (mot de
passe DB + JWT générés), CloudWatch, ALB, rôles IAM + OIDC GitHub, service ECS.
Le service démarre avec l'image placeholder → **statut UNHEALTHY** jusqu'au
premier vrai déploiement (étape 3). C'est attendu.

Récupérer les valeurs pour GitHub :

```bash
terraform output                                  # vue d'ensemble
terraform output -raw github_deploy_role_arn      # -> AWS_DEPLOY_ROLE_ARN
terraform output -raw ecr_repository_name         # -> ECR_REPOSITORY
terraform output -raw ecs_cluster_name            # -> ECS_CLUSTER
terraform output -raw ecs_service_name            # -> ECS_SERVICE
terraform output -raw ecs_task_family             # -> ECS_TASK_FAMILY
```

---

## 2. Configurer le dépôt GitHub (CD)

Dans **Settings → Secrets and variables → Actions**, créer ces **secrets** :

| Secret GitHub | Valeur (source) |
|---|---|
| `AWS_DEPLOY_ROLE_ARN` | `terraform output -raw github_deploy_role_arn` |
| `ECR_REPOSITORY` | `terraform output -raw ecr_repository_name` |
| `ECS_CLUSTER` | `terraform output -raw ecs_cluster_name` |
| `ECS_SERVICE` | `terraform output -raw ecs_service_name` |
| `ECS_TASK_FAMILY` | `terraform output -raw ecs_task_family` |

Puis, dans **Settings → Environments**, créer un environnement nommé
**`production`** (le workflow le cible, et la trust policy OIDC l'autorise).

> Lancer `terraform apply` **avant** de déclencher le workflow : il crée la
> famille de task definition que le pipeline va lire.

---

## 3. Premier déploiement (vraie image)

Le plus simple : déclencher le workflow **Deploy**.

```bash
# soit en poussant sur main (déclenche le workflow)
git push origin main
# soit manuellement : onglet Actions → "Deploy" → "Run workflow"
```

Le pipeline : OIDC → build de l'image → scan Trivy (bloque si CRITICAL/HIGH) →
push ECR taggé par SHA → enregistre une révision de task def → met à jour le
service et attend la stabilité.

*(Alternative 100 % manuelle : voir la section « Push a first image » du
[`README.md`](./README.md).)*

---

## 4. Vérifier

```bash
ALB=$(cd infra && terraform output -raw alb_dns_name)

# HTTP (sans certificat ACM) :
curl -fsS "http://${ALB}/actuator/health"      # -> {"status":"UP"}
# HTTPS (avec certificat + domaine pointant sur l'ALB) :
# curl -fsS "https://api.ton-domaine/actuator/health"
```

Dans CloudWatch Logs (`/ecs/spoony-prod`), confirmer la ligne
`The following profiles are active: prod` et que Flyway applique V1..V7.

---

## 5. Coût (estimation mensuelle, eu-west-3)

| Poste | ~ €/mois |
|---|---:|
| Fargate (0.5 vCPU / 1 Go, 1 tâche) | ~18 |
| ALB | ~18 |
| RDS db.t4g.micro (20 Go gp3) | ~13 |
| Logs / Secrets / ECR + IPv4 publiques | ~4-8 |
| NAT gateway (aucun, par choix) | 0 |
| **Total** | **~50-55** |

---

## 6. À faire après la V0 (non bloquant)

- **HTTPS obligatoire** : fournir `acm_certificate_arn` + domaine (le HTTP:80
  bootstrap ne doit pas servir de données de santé).
- **State distant chiffré** : migrer vers le backend S3 (SSE-KMS) + lock
  DynamoDB — le state contient le mot de passe DB et le JWT **en clair**.
- **Utilisateur DB dédié** least-privilege (la V0 utilise le master RDS).
- **Observabilité** : export métriques Micrometer → CloudWatch + alarmes (5xx,
  latence, CPU), séparation liveness/readiness, sécurisation de `/actuator`.
- **Haute dispo** : `desired_count >= 2`, autoscaling, RDS multi-AZ.

Détails et justifications dans [`README.md`](./README.md).
