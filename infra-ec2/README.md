# Spoony backend — AWS EC2 low-cost beta

**Date de préparation : 10 septembre 2026**

**Région :** `eu-west-3` (Paris)

**API :** `https://api.spoonrest.martincharrier.dev`

Cette stack remplace la cible ECS/RDS comme chemin de déploiement actif pour la
bêta économique. L'ancienne stack `infra/` est conservée comme cible de
migration future, mais elle ne doit pas être appliquée pour cette phase.

Pour un compte créé depuis le 15 juillet 2025, `t4g.small` est éligible au plan
gratuit à crédits. Elle consomme néanmoins ces crédits avec EBS, S3 et l'adresse
IPv4 : l'architecture n'est pas gratuite après leur expiration.

## Architecture

```text
Cloudflare DNS
      │
      ▼
Elastic IP ── EC2 t4g.small ARM64
                 ├── Caddy : 80/443 publics, TLS automatique
                 ├── Spring Boot : réseau Docker uniquement
                 └── PostgreSQL : réseau Docker interne uniquement
                           │
                           ├── volume EBS gp3 chiffré et protégé
                           └── dump quotidien chiffré dans S3
```

L'administration passe par AWS Systems Manager. Aucun port SSH, PostgreSQL ou
Spring n'est ouvert dans le security group.

## Ressources créées

- VPC, Internet Gateway, subnet public et security group ;
- une EC2 `t4g.small` avec crédits CPU en mode `standard` ;
- un disque système gp3 chiffré de 10 Gio ;
- un disque de données gp3 chiffré de 20 Gio, protégé contre la destruction ;
- une Elastic IP ;
- un bucket S3 privé/versionné pour `releases/` et `backups/` ;
- quatre paramètres chiffrés SSM : administrateur DB, migrateur, runtime et JWT ;
- un rôle EC2 à privilèges minimaux ;
- un rôle GitHub Actions OIDC, sans clé AWS permanente.

Le bucket d'état Terraform est créé séparément par `bootstrap/`, car un backend
Terraform doit exister avant de pouvoir stocker son propre état.

## Déploiement

Le workflow `.github/workflows/deploy.yml` :

1. attend la réussite de la CI sur `main` ;
2. construit réellement une image `linux/arm64` ;
3. bloque les vulnérabilités HIGH/CRITICAL corrigeables avec Trivy ;
4. place une archive immuable dans S3, sans registre Docker payant ;
5. déploie par SSM, sans SSH ;
6. démarre PostgreSQL, exécute le bootstrap des rôles et Flyway ;
7. démarre Spring et Caddy ;
8. effectue un smoke-test HTTPS ;
9. remet l'image précédente si le service ou le smoke-test échoue.

Le rollback applicatif ne revient jamais en arrière sur une migration SQL. Les
migrations doivent donc rester compatibles avec la version précédente selon le
principe expand/contract.

## Sauvegardes

`spoony-backup.timer` lance chaque jour un `pg_dump` au format custom, l'envoie
chiffré dans `s3://<bucket>/backups/database/`, puis vérifie l'objet distant.
Les dumps expirent après 35 jours par défaut. Le workflow
`backup-check.yml` échoue si aucun dump n'a moins de 36 heures, ce qui rend
l'incident visible dans GitHub Actions.

Avant toute donnée réelle, un dump doit être restauré dans une base temporaire
et vérifié. La présence d'un fichier dans S3 ne suffit pas à prouver qu'il est
restaurable.

## Limites assumées de cette bêta

- une seule machine et une seule zone de disponibilité ;
- une panne EC2 interrompt l'API et PostgreSQL ;
- RPO maximal d'environ 24 heures avec les dumps quotidiens ;
- maintenance OS et PostgreSQL à planifier ;
- capacité mémoire limitée à 2 Gio ;
- le volume EBS protège d'une recréation de l'instance, pas d'une panne de zone.

Ce niveau est adapté à une bêta contrôlée avec peu d'utilisateurs. Avant une
production critique ou une charge significative, migrer PostgreSQL vers RDS et
reconsidérer la haute disponibilité.

## Validation locale

```bash
terraform -chdir=infra-ec2 validate
terraform -chdir=infra-ec2 test
terraform -chdir=infra-ec2/bootstrap validate

docker compose -f infra-ec2/docker-compose.production.yml config --quiet
bash infra-ec2/tests/validate-compose.sh
./mvnw verify
```

Le runbook opératoire complet est dans [`DEPLOY.md`](./DEPLOY.md).
