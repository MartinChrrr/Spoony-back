# Déploiement EC2 — checklist pas-à-pas

Ne jamais exécuter `terraform apply` dans l'ancien dossier `infra/` pour la
bêta low-cost. Toutes les commandes suivantes partent de la racine du backend.

Le compte vérifié utilise le plan AWS `FREE` à crédits. `t4g.small` y est
éligible mais consomme les crédits avec EBS, S3 et l'adresse IPv4 ; surveiller
leur solde et leur date d'expiration reste obligatoire.

## 1. Authentifier AWS

```bash
aws login
aws sts get-caller-identity
aws configure set credential_process \
  "aws configure export-credentials --profile default --format process" \
  --profile terraform
aws configure set region eu-west-3 --profile terraform
aws sts get-caller-identity --profile terraform
```

Vérifier que le compte retourné est exactement celui attendu.

## 2. Créer uniquement le bucket d'état

```bash
cd infra-ec2/bootstrap
cp terraform.tfvars.example terraform.tfvars
```

Dans `terraform.tfvars`, remplacer l'ID de compte et choisir un nom de bucket
globalement unique, par exemple `spoony-terraform-state-<id-compte>`.

```bash
AWS_PROFILE=terraform terraform init
AWS_PROFILE=terraform terraform plan -out=bootstrap.tfplan
AWS_PROFILE=terraform terraform apply bootstrap.tfplan
```

Ce premier apply crée un seul bucket S3 privé, chiffré et versionné.

## 3. Initialiser la stack principale

```bash
cd ..
cp backend-prod.hcl.example backend-prod.hcl
cp terraform.tfvars.example terraform.tfvars
```

Renseigner :

- `backend-prod.hcl` : le bucket créé à l'étape 2 ;
- `aws_account_id` : l'ID confirmé par STS ;
- `acme_email` : l'adresse qui recevra les notifications de certificat ;
- `cors_allowed_origins` : conserver une liste HTTPS sans `*` ;
- `github_oidc_provider_arn` : uniquement si ce provider existe déjà.

```bash
AWS_PROFILE=terraform terraform init -backend-config=backend-prod.hcl
AWS_PROFILE=terraform terraform plan -out=ec2.tfplan
```

Relire le plan. Il doit contenir une EC2 `t4g.small`, deux volumes EBS pour un
total de 30 Gio, une Elastic IP, un bucket d'artefacts, IAM, SSM et le réseau.
Il ne doit contenir aucun RDS, ALB, ECS, NAT Gateway ou Route 53.

Ne lancer l'apply qu'après validation explicite du plan et du coût :

```bash
AWS_PROFILE=terraform terraform apply ec2.tfplan
```

## 4. Attendre le bootstrap de la machine

```bash
INSTANCE_ID=$(AWS_PROFILE=terraform terraform output -raw instance_id)
aws ssm describe-instance-information \
  --region eu-west-3 \
  --query "InstanceInformationList[?InstanceId=='$INSTANCE_ID'].PingStatus"
```

Le résultat attendu est `Online`. En cas d'échec, consulter
`/var/log/spoony-bootstrap.log` avec une session SSM, jamais en ouvrant SSH.

## 5. Configurer Cloudflare

Récupérer l'IP :

```bash
AWS_PROFILE=terraform terraform output -raw public_ip
```

Dans Cloudflare, créer :

```text
Type   : A
Nom    : api.spoonrest
Cible  : <public_ip>
Proxy  : DNS only au premier déploiement
TTL    : Auto
```

Vérifier que le nom renvoie l'Elastic IP avant de lancer Caddy :

```bash
dig +short api.spoonrest.martincharrier.dev
```

## 6. Configurer GitHub

Dans **Settings → Environments**, créer `production`.

Ajouter un secret d'environnement :

| Nom | Valeur |
|---|---|
| `AWS_DEPLOY_ROLE_ARN` | `terraform output -raw github_deploy_role_arn` |

Ajouter les variables d'environnement :

| Nom | Valeur |
|---|---|
| `AWS_ARTIFACT_BUCKET` | `terraform output -raw artifact_bucket` |
| `AWS_PARAMETER_PREFIX` | `terraform output -raw ssm_parameter_prefix` |
| `EC2_INSTANCE_ID` | `terraform output -raw instance_id` |
| `API_DOMAIN` | `api.spoonrest.martincharrier.dev` |
| `API_BASE_URL` | `https://api.spoonrest.martincharrier.dev` |
| `ACME_EMAIL` | même adresse que dans Terraform |
| `CORS_ALLOWED_ORIGINS` | même valeur que dans Terraform |

Ne créer aucune access key AWS pour GitHub : OIDC est déjà prévu.

## 7. Premier déploiement

Avec uniquement des données fictives :

1. ouvrir GitHub Actions ;
2. choisir **Deploy EC2** ;
3. cliquer **Run workflow** sur `main` ;
4. suivre build ARM64, Trivy, S3, SSM, migration et smoke-test.

Le certificat TLS est demandé automatiquement par Caddy. ACM n'intervient pas.

## 8. Vérifications après déploiement

```bash
curl -fsS https://api.spoonrest.martincharrier.dev/actuator/health/liveness
curl -fsS https://api.spoonrest.martincharrier.dev/actuator/health/readiness
```

Puis tester : inscription, login, refresh JWT, logout, isolation de deux comptes,
redémarrage applicatif, redémarrage EC2 et persistance des données.

## 9. Tester le backup et la restauration

Lancer le backup par SSM :

```bash
aws ssm send-command \
  --instance-ids "$INSTANCE_ID" \
  --document-name AWS-RunShellScript \
  --parameters 'commands=["systemctl start spoony-backup.service"]' \
  --region eu-west-3
```

Vérifier le dump :

```bash
BUCKET=$(AWS_PROFILE=terraform terraform output -raw artifact_bucket)
aws s3 ls "s3://$BUCKET/backups/database/" --region eu-west-3
```

La restauration doit être faite dans une base temporaire isolée. La base
temporaire doit appartenir au rôle migrateur ; restaurer avec ce rôle, puis
relancer le conteneur `migration` contre cette base afin de valider Flyway et de
réappliquer les droits runtime. Ne jamais écraser la base active pour tester une
sauvegarde. Ce chemin est exécuté automatiquement par
`infra-ec2/tests/validate-compose.sh` lors de la validation locale.

## 10. Rollback applicatif

Le pipeline déclenche automatiquement le rollback si le nouveau conteneur ou le
smoke-test HTTPS échoue. Le test manuel, avec des données fictives, consiste à
déployer une image volontairement non saine et à confirmer que l'image
précédente redevient `healthy`.

Les migrations SQL ne sont pas annulées : elles doivent être compatibles avec
les deux versions applicatives.
