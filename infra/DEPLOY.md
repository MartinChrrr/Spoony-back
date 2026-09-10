# Déploiement AWS V0 — guide pas-à-pas

Mise en ligne du backend Spoony sur **AWS ECS Fargate** en **eu-west-3 (Paris)**.
Ce fichier est la checklist actionnable ; voir [`README.md`](./README.md) pour
l'architecture détaillée, les coûts et la dette post-V0.

> ⚠️ Aucune ressource AWS n'est créée tant que **tu** ne lances pas `terraform
> apply` avec **ton** compte. Cela génère des coûts (~50-55 €/mois, cf. plus bas).

---

## 0. Prérequis (à installer une fois)

```bash
# Terraform >= 1.10 (verrouillage natif S3 via use_lockfile)
terraform -version

# AWS CLI v2 + session temporaire (éviter les clés IAM longue durée)
aws --version
aws login              # ouvre le navigateur et crée une session temporaire
aws sts get-caller-identity   # doit afficher ton compte

# Le provider Terraform AWS v5 ne lit pas encore directement login_session.
# Créer une fois ce profil relais, qui n'enregistre aucune clé longue durée :
aws configure set credential_process \
  "aws configure export-credentials --profile default --format process" \
  --profile terraform
aws configure set region eu-west-3 --profile terraform
aws sts get-caller-identity --profile terraform

# Docker (pour la 1re image si tu ne passes pas par le pipeline)
docker --version

# jq (utilisé par le bootstrap manuel d'image)
jq --version
```

Obligatoire pour `environment="prod"` : un **domaine** et un **certificat ACM
dans eu-west-3**. Terraform refuse désormais de créer une production HTTP.

---

## 1. Créer le backend Terraform sécurisé

Le bucket de state doit exister avant la stack qu'il décrit. Choisir un nom
globalement unique, puis activer chiffrement, versioning, blocage public et
verrouillage natif S3 :

```bash
STATE_BUCKET="remplacer-par-un-nom-unique"
aws s3api create-bucket \
  --bucket "$STATE_BUCKET" \
  --region eu-west-3 \
  --create-bucket-configuration LocationConstraint=eu-west-3
aws s3api put-bucket-encryption \
  --bucket "$STATE_BUCKET" \
  --server-side-encryption-configuration \
  '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'
aws s3api put-bucket-versioning \
  --bucket "$STATE_BUCKET" \
  --versioning-configuration Status=Enabled
aws s3api put-public-access-block \
  --bucket "$STATE_BUCKET" \
  --public-access-block-configuration \
  BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
```

Copier `backend-prod.hcl.example` vers `backend-prod.hcl`, renseigner le bucket,
puis initialiser avec `terraform init -backend-config=backend-prod.hcl`.

## 2. Provisionner l'infrastructure sans démarrer le service

```bash
cd infra

AWS_PROFILE=terraform terraform init -backend-config=backend-prod.hcl

cp terraform.tfvars.example terraform.tfvars
# Éditer terraform.tfvars :
#   - aws_account_id       (OBLIGATOIRE — valeur retournée par STS)
#   - cors_allowed_origins  (OBLIGATOIRE — origines de l'app, séparées par des virgules)
#   - acm_certificate_arn   (OBLIGATOIRE en production)
#   - alarm_email           (recommandé, puis confirmer l'e-mail AWS)
#   - laisser desired_count=0 pour ce premier apply sûr
#   - si un provider OIDC GitHub existe déjà dans le compte, renseigner son ARN

AWS_PROFILE=terraform terraform plan   # relire ce qui va être créé
AWS_PROFILE=terraform terraform apply  # taper "yes"
```

Crée : VPC (sans NAT), RDS PostgreSQL chiffrée, ECR, Secrets Manager (mots de
passe DB administrateur/migration/runtime + JWT générés), CloudWatch, ALB,
rôles IAM + OIDC GitHub, service ECS.
Le service ECS existe mais reste à zéro tâche : aucun conteneur factice instable
n'est démarré et le premier `apply` peut se terminer proprement.

Récupérer les valeurs pour GitHub :

```bash
terraform output                                  # vue d'ensemble
terraform output -raw github_deploy_role_arn      # -> AWS_DEPLOY_ROLE_ARN
terraform output -raw ecr_repository_name         # -> ECR_REPOSITORY
terraform output -raw ecs_cluster_name            # -> ECS_CLUSTER
terraform output -raw ecs_service_name            # -> ECS_SERVICE
terraform output -raw ecs_task_family             # -> ECS_TASK_FAMILY
terraform output -raw ecs_migration_task_family   # -> ECS_MIGRATION_TASK_FAMILY
```

---

## 3. Configurer le dépôt GitHub (CD)

Dans **Settings → Secrets and variables → Actions**, créer ces **secrets** :

| Secret GitHub | Valeur (source) |
|---|---|
| `AWS_DEPLOY_ROLE_ARN` | `terraform output -raw github_deploy_role_arn` |
| `ECR_REPOSITORY` | `terraform output -raw ecr_repository_name` |
| `ECS_CLUSTER` | `terraform output -raw ecs_cluster_name` |
| `ECS_SERVICE` | `terraform output -raw ecs_service_name` |
| `ECS_TASK_FAMILY` | `terraform output -raw ecs_task_family` |
| `ECS_MIGRATION_TASK_FAMILY` | `terraform output -raw ecs_migration_task_family` |
| `API_BASE_URL` | URL HTTPS publique, ex. `https://api.spoony.martincharrier.dev` |

Puis, dans **Settings → Environments**, créer un environnement nommé
**`production`** (le workflow le cible, et la trust policy OIDC l'autorise).
Ajouter si nécessaire la variable `ECS_DESIRED_COUNT` ; sa valeur par défaut est
`1`, adaptée uniquement à une bêta contrôlée. L'abonnement SNS envoyé à
`alarm_email` doit aussi être confirmé.

> Lancer `terraform apply` **avant** de déclencher le workflow : il crée la
> famille de task definition que le pipeline va lire.

---

## 4. Premier déploiement (vraie image)

Le plus simple : pousser sur `main`. Le workflow **Deploy** ne démarre qu'après
la réussite complète de **CI** (tests H2, PostgreSQL 16, image et Terraform),
remplace l'image factice, scale ensuite le service, exécute un smoke test HTTPS
et restaure la task definition précédente si le smoke test échoue.

```bash
# soit en poussant sur main (déclenche le workflow)
git push origin main
# soit manuellement sur main : onglet Actions → "Deploy" → "Run workflow"
```

Le pipeline : OIDC → build de l'image → scan Trivy (bloque si CRITICAL/HIGH) →
push ECR taggé par SHA → exécute la tâche ponctuelle de bootstrap/migration DB →
enregistre une révision de task def web → met à jour et scale le service →
attend la stabilité → smoke test HTTPS → rollback applicatif si besoin.

> Une migration de base n'est pas annulée par le rollback ECS. Toute migration
> doit donc rester compatible avec la version applicative précédente selon une
> stratégie expand/contract.

Le chemin de production supporté est ce workflow complet. Un simple push manuel
de l'image contournerait la tâche de migration et ne doit pas démarrer le service.

---

## 5. Vérifier

```bash
ALB=$(cd infra && terraform output -raw alb_dns_name)

curl -fsS "https://api.ton-domaine/actuator/health/readiness"
curl -fsS "https://api.ton-domaine/actuator/health/liveness"
```

Dans CloudWatch Logs (`/ecs/spoony-prod`), confirmer dans le flux `migration`
que Flyway applique/valide toutes les migrations et termine avec succès, puis
dans le flux `app` que le profil `prod` est actif et que le service devient prêt.

---

## 6. Coût (estimation mensuelle, eu-west-3)

| Poste | ~ €/mois |
|---|---:|
| Fargate (0.5 vCPU / 1 Go, 1 tâche) | ~18 |
| ALB | ~18 |
| RDS db.t4g.micro (20 Go gp3) | ~13 |
| Logs / métriques / alarmes / Secrets / ECR + IPv4 publiques | ~5-10 |
| NAT gateway (aucun, par choix) | 0 |
| **Total** | **~51-57** |

---

## 7. Actions manuelles restantes

- **Reprise** : effectuer et consigner un test réel de restauration RDS.
- **Haute dispo** : `desired_count >= 2`, autoscaling, RDS multi-AZ.

Détails et justifications dans [`README.md`](./README.md).
