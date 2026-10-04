# API V0 — mettre en ligne `api.spoony.martincharrier.dev` (Cloudflare + ACM + ALB)

Runbook du branchement domaine pour l'API Spoony, décidé en **ADR-022**.
Complète [`DEPLOY.md`](./DEPLOY.md) (qui décrit l'`apply` AWS) côté **DNS + TLS**.

- **Sous-domaine API :** `api.spoony.martincharrier.dev`
- **DNS :** Cloudflare (zone `martincharrier.dev`)
- **Hébergement :** ECS Fargate / ALB, **eu-west-3** (inchangé, ADR-020)

> ⚠️ **Ordre imposé par un œuf-et-poule :** l'enregistrement final ne peut pointer
> vers l'ALB qu'une fois l'infra `apply`. En revanche le **certificat ACM se
> demande et se valide AVANT** l'ALB (validation = preuve de possession du
> domaine, indépendante de l'ALB). D'où la séquence ci-dessous.

---

## 0. Zone Cloudflare active

Dans Cloudflare → **martincharrier.dev** doit être en statut **Active**.
Si le domaine a été acheté ailleurs que chez Cloudflare : pointer les
**nameservers** du registrar sur ceux fournis par Cloudflare, puis attendre
l'activation (quelques minutes à quelques heures).

## 1. Outillage local (une fois — `sudo`)

```bash
# Terraform (dépôt HashiCorp, Fedora)
sudo dnf install -y dnf-plugins-core
sudo dnf config-manager addrepo --from-repofile=https://rpm.releases.hashicorp.com/fedora/hashicorp.repo
sudo dnf install -y terraform

# AWS CLI v2
curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o /tmp/awscliv2.zip
cd /tmp && unzip -q awscliv2.zip && sudo ./aws/install

# jq (bootstrap manuel d'image, cf. README)
sudo dnf install -y jq

aws login              # session temporaire via le navigateur
aws sts get-caller-identity   # doit afficher ton compte

# Pont compatible avec le provider Terraform, sans clé longue durée :
aws configure set credential_process \
  "aws configure export-credentials --profile default --format process" \
  --profile terraform
aws configure set region eu-west-3 --profile terraform
```

## 2. Certificat ACM (en `eu-west-3`, **avant** l'ALB)

> Le cert **doit** être dans **eu-west-3** (même région que l'ALB). PAS us-east-1
> (ça, c'est pour CloudFront — on n'en a pas).

```bash
aws acm request-certificate \
  --domain-name api.spoony.martincharrier.dev \
  --validation-method DNS \
  --region eu-west-3
# -> renvoie le CertificateArn ; récupérer le CNAME de validation :
aws acm describe-certificate --region eu-west-3 \
  --certificate-arn <ARN> \
  --query 'Certificate.DomainValidationOptions[0].ResourceRecord'
```
(Ou via la console : **Certificate Manager → région Paris → Request → Public →
DNS validation**.)

ACM fournit un enregistrement **CNAME de validation** (`Name` = `_xxxx.api.spoony…`,
`Value` = `_yyyy.….acm-validations.aws.`). Dans **Cloudflare → DNS → Add record** :

| Champ | Valeur |
|---|---|
| Type | `CNAME` |
| Name | la partie hôte du `Name` ACM **relative à la zone** (Cloudflare ajoute `martincharrier.dev` tout seul — ne le retape pas) |
| Target | le `Value` ACM (garde-le tel quel) |
| Proxy status | **DNS only (nuage GRIS)** — un enregistrement de validation ne se proxifie jamais |
| TTL | Auto |

Attendre que le cert passe **`Issued`** (quelques minutes). **Copier le
`CertificateArn`.**

## 3. `terraform apply`

`infra/terraform.tfvars` est déjà pré-rempli. Y coller l'ARN du cert :

```hcl
acm_certificate_arn = "arn:aws:acm:eu-west-3:<account>:certificate/<id>"
```

```bash
cd infra
AWS_PROFILE=terraform terraform init -backend-config=backend-prod.hcl
AWS_PROFILE=terraform terraform plan
AWS_PROFILE=terraform terraform apply        # "yes"
terraform output -raw alb_dns_name   # -> spoony-prod-alb-xxxx.eu-west-3.elb.amazonaws.com
```

Le cert étant fourni, l'ALB crée le **listener HTTPS:443** (+ redirect HTTP:80→443).
Le service ECS reste à **zéro tâche** pendant le bootstrap : aucune image
placeholder ne démarre. Le premier workflow CD remplace l'image puis scale le
service (cf. `DEPLOY.md §3`).

## 4. Pointer le sous-domaine sur l'ALB

Dans **Cloudflare → DNS → Add record** :

| Champ | Valeur |
|---|---|
| Type | `CNAME` |
| Name | `api.spoony` |
| Target | le `alb_dns_name` de l'étape 3 |
| Proxy status | **DNS only (nuage GRIS)** en V0 |
| TTL | Auto |

> **Pourquoi nuage gris ?** Le proxy orange de Cloudflare devant un ALB HTTPS+ACM
> empile deux couches TLS (cert edge Cloudflare ≠ cert ACM origin) : faisable en
> mode *Full (strict)* mais ça complique le debug pour rien en V0. Le proxy
> (WAF, cache, masquage IP) est une **amélioration post-V0**, pas un prérequis.

## 5. Déployer la vraie image + vérifier

Secrets GitHub + environnement `production` puis workflow **Deploy**
(cf. `DEPLOY.md §2-3`). Ensuite :

```bash
curl -fsS https://api.spoony.martincharrier.dev/actuator/health   # -> {"status":"UP"}
```

## 6. Brancher le front

`spoony-frontend/eas.json` → profil **production** :

```json
"API_BASE_URL": "https://api.spoony.martincharrier.dev"
```

Décider le staging : soit `https://api-staging.spoony.martincharrier.dev`
(2e cert + 2e enregistrement), soit le retirer pour la V0. Si un jour un origin
**web** appelle l'API depuis un navigateur, l'ajouter à `cors_allowed_origins`
dans `terraform.tfvars` puis `terraform apply` (l'app **mobile** RN n'envoie pas
d'`Origin` → CORS sans effet sur elle).

---

## Gotchas (résumé)

- **ACM en eu-west-3**, pas us-east-1.
- **Validation ACM ET enregistrement final = DNS only (nuage gris)** en V0.
- **`.dev` = HSTS preload** → HTTPS obligatoire dans tout navigateur, aucun
  fallback HTTP. Le cert doit donc être en place avant tout usage réel.
- `terraform.tfvars` est **gitignore** (discipline : aucune valeur sensible en
  clair dans le repo ; les secrets applicatifs vivent dans Secrets Manager).
- `spoonrest.app` reste la cible **post-clearance de marque** (ADR-016) : il
  pourra être ajouté en **SAN** au même certificat/ALB sans rien refaire.
