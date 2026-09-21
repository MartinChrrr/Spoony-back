# Spoony backend — ancienne cible ECS/RDS (migration future)

> **Ne pas appliquer pour la bêta low-cost.** Depuis le 10 septembre 2026, le
> chemin de déploiement actif est `../infra-ec2/`. Cette stack est conservée
> comme cible de migration future vers ECS/RDS.

Cost-optimised V0 stack for the Spoony backend (Spring Boot 3.5.11, Java 21,
port 8080, Spring profile `prod`, PostgreSQL) on **AWS ECS Fargate** in
**eu-west-3 (Paris)**.

## Architecture overview

```
Internet
   │  80 / 443
   ▼
[ ALB ]  (public subnets, SG: alb)
   │  HTTP 8080  (SG: app allows only the ALB)
   ▼
[ Fargate task ]  (PUBLIC subnets, public IP, SG: app)
   │  egress via Internet Gateway -> ECR / Logs / Secrets Manager
   │  5432  (SG: rds allows only the app)
   ▼
[ RDS PostgreSQL 16 ]  (PRIVATE subnets, no Internet route, encrypted)
```

No NAT gateway: the task lives in a public subnet (egress through the IGW) and
is firewalled by its security group; RDS lives in private subnets and never
needs egress. This trades a little exposure surface for roughly **-32 €/mo**.

## Prerequisites

- An AWS account with admin (or sufficient) rights to create VPC/IAM/ECS/RDS.
- `terraform` >= 1.10
- `aws-cli` v2, authenticated with a short-lived session (`aws login`) for
  **eu-west-3**; avoid long-lived IAM access keys. Configure the local
  `terraform` credential-process profile shown in `DEPLOY.md`, because the AWS
  provider version pinned here does not read `login_session` directly.
- `docker` (to build/push the first image, if not using the pipeline).
- A domain and an **ACM certificate in eu-west-3** for production; Terraform
  rejects `environment="prod"` when `acm_certificate_arn` is empty.
- A versioned, encrypted, private S3 bucket for Terraform state.
- The expected AWS account ID in `aws_account_id`; the provider refuses every
  other account to prevent an accidental deployment with the wrong session.

## Files

| File | Purpose |
|------|---------|
| `versions.tf` | Terraform/provider versions and mandatory S3 backend with native lockfile. |
| `variables.tf` | Inputs + `local.name_prefix = "${project_name}-${environment}"`. |
| `terraform.tfvars.example` | Sample values — copy to `terraform.tfvars`. |
| `network.tf` | VPC, IGW, 2 public + 2 private subnets, routes, 3 security groups. |
| `ecr.tf` | ECR repo (immutable, scan-on-push, keep last 10 images). |
| `secrets.tf` | Separate administrator, Flyway, runtime DB passwords + JWT secret in Secrets Manager. |
| `rds.tf` | RDS PostgreSQL 16, encrypted, force-SSL parameter group. |
| `logs.tf` | CloudWatch log group `/ecs/<prefix>`. |
| `monitoring.tf` | SNS topic and core ALB/ECS/RDS/certificate alarms. |
| `iam.tf` | ECS execution/task roles, GitHub OIDC provider + deploy role. |
| `alb.tf` | ALB, target group, conditional HTTP/HTTPS listeners. |
| `ecs.tf` | Cluster, one-shot migration task, least-privilege web task and service. |
| `outputs.tf` | Values consumed by the pipeline and this runbook. |

## Deployment order

### 1. Initialise Terraform

```bash
cd infra
cp backend-prod.hcl.example backend-prod.hcl
# edit backend-prod.hcl with the pre-created state bucket
AWS_PROFILE=terraform terraform init -backend-config=backend-prod.hcl
```

The bucket bootstrap commands are in `DEPLOY.md`. State is encrypted and
versioned in S3, with Terraform's native `use_lockfile` locking. Local state is
not an accepted production mode.

### 2. Apply

```bash
cp terraform.tfvars.example terraform.tfvars
# edit terraform.tfvars: cors_allowed_origins (required), acm_certificate_arn, etc.
AWS_PROFILE=terraform terraform apply
```

This creates the VPC, ECR, RDS, secrets, ALB and ECS service. Keep
`desired_count=0` for this bootstrap apply: the task definition may reference
the placeholder, but no fake task is started. The first successful CD workflow
deploys a real image before scaling the service. Terraform refuses a non-zero
desired count with the placeholder image.

Note the outputs:

```bash
terraform output
terraform output -raw github_deploy_role_arn   # -> GitHub secret AWS_DEPLOY_ROLE_ARN
terraform output -raw ecr_repository_url
```

### 3. Deploy the first image

Use the GitHub **Deploy** workflow, manually via *Run workflow* after setting the
repository secrets below. It is the supported production path because it keeps
the required order atomic at pipeline level: build, vulnerability scan, push,
one-shot role bootstrap/Flyway migration, web rollout, HTTPS smoke test and
application rollback.

Do not start the service after a simple manual image push: that would bypass the
migration gate and the least-privilege runtime role might not exist yet. If an
emergency manual deployment is ever needed, reproduce every step of
`.github/workflows/deploy.yml`, including the migration task and its exit-code
check, rather than updating the web service directly.

### 4. Wait for stability

The migration task must exit with code 0 first. The web task then returns 200 on
`/actuator/health/readiness` after its cold start (allow roughly 1–2 minutes).

```bash
aws ecs wait services-stable \
  --cluster "$(terraform output -raw ecs_cluster_name)" \
  --services "$(terraform output -raw ecs_service_name)" \
  --region eu-west-3
```

### 5. Smoke test

```bash
ALB=$(terraform output -raw alb_dns_name)
curl -fsS "https://<your-domain>/actuator/health/readiness"
```

## GitHub configuration (CD pipeline)

The workflow `.github/workflows/deploy.yml` authenticates to AWS via OIDC (no
long-lived keys). Create these **repository secrets** (Settings → Secrets and
variables → Actions), all sourced from Terraform outputs:

| Secret | Value (source) |
|--------|----------------|
| `AWS_DEPLOY_ROLE_ARN` | `terraform output -raw github_deploy_role_arn` |
| `ECR_REPOSITORY` | repo **name** only, i.e. `spoony-backend` (the URL is derived from the ECR login registry) |
| `ECS_CLUSTER` | `terraform output -raw ecs_cluster_name` |
| `ECS_SERVICE` | `terraform output -raw ecs_service_name` |
| `ECS_TASK_FAMILY` | `terraform output -raw ecs_task_family` (e.g. `spoony-prod-app`) |
| `ECS_MIGRATION_TASK_FAMILY` | `terraform output -raw ecs_migration_task_family` |
| `API_BASE_URL` | Public HTTPS API origin used by the post-deploy smoke test |

Also create a protected GitHub **Environment** named `production` (the workflow
targets it, and the OIDC trust policy allows `environment:production`). The
deploy starts automatically only after the complete CI workflow succeeds on
`main`; its HTTPS smoke test triggers an ECS rollback on failure.

> Run `terraform apply` **before** triggering the Deploy workflow: it creates the
> ECS task-definition family that the workflow's `describe-task-definition` step
> reads. Triggering Deploy first fails because the family does not exist yet.

> `ECR_REPOSITORY` is the bare repository name. The registry host comes from the
> `amazon-ecr-login` action output, so the full pushed reference is
> `<registry>/spoony-backend:<sha>`.

## Cost (rough monthly estimate, eu-west-3)

| Component | Spec | ~ €/mo |
|-----------|------|-------:|
| Fargate | 0.5 vCPU / 1 GiB, 1 task, 24/7 | ~18 |
| ALB | 1 ALB + minimal LCU | ~18 |
| RDS | db.t4g.micro, 20 GiB gp3, single-AZ | ~13 |
| CloudWatch Logs / Secrets / ECR | low volume | ~1–3 |
| Public IPv4 addresses | ALB + Fargate task (AWS charges since Feb 2024) | ~3–7 |
| **NAT gateway** | **none (by design)** | **0** |
| **Total** | | **~50–55 €/mo** |

Figures are indicative (on-demand, excl. data transfer/VAT). Fargate Spot or a
scheduled scale-to-zero could lower the V0 cost further.

## Security / TODO post-V0

- **Separated database roles.** The CD runs a short-lived migration task with
  administrator + Flyway credentials. It creates/rotates the migrator and
  runtime roles, applies Flyway, then grants DML only. The web task receives
  only the runtime secret and starts with Flyway disabled. Database migrations
  must remain backward-compatible because ECS rollback does not undo SQL.
- **Secret rotation.** Enable Secrets Manager rotation for the DB password
  (AWS PostgreSQL single-user rotation Lambda). The JWT secret needs an in-app
  multi-key (`kid`) strategy before it can be rotated without logging everyone
  out — see TODO in `secrets.tf`.
- **HTTPS mandatory.** Provide `acm_certificate_arn` so traffic is TLS and HTTP
  redirects to HTTPS. Plain HTTP:80 (empty cert) is for bootstrap only and must
  not serve real users / health data.
- **Remove public IPs from tasks.** Move the tasks into private subnets and add
  VPC interface endpoints (ECR api/dkr, Logs, Secrets Manager, STS) + an S3
  gateway endpoint, so no Fargate task is ever Internet-reachable. (Adds endpoint
  cost but no NAT.)
- **High availability.** `multi_az = true` on RDS, `desired_count >= 2` and an
  Application Auto Scaling target tracking policy on the ECS service.
- **Remote state access.** Restrict the state bucket policy to the deployment
  administrators and CI role; S3 encryption/versioning/lockfiles protect the
  state, but authorized readers can still see generated secret values.
- **Strict DB TLS.** `sslmode=require` encrypts the RDS connection but does not
  verify the server certificate. Move to `sslmode=verify-full` with the
  `rds-ca-rsa2048-g1` CA bundle post-V0. Acceptable for the V0.

## Health data / GDPR notes

- **EU region only**: all compute and data reside in **eu-west-3 (Paris)**.
- **Encryption at rest**: RDS `storage_encrypted = true`; ECR images encrypted.
- **Encryption in transit**: `rds.force_ssl = 1` server-side + `sslmode=require`
  in `DATABASE_URL`; enable the HTTPS listener for client traffic.
- **Backups**: automated RDS backups retained **7 days**;
  `deletion_protection = true` and a final snapshot on destroy guard against
  accidental data loss.
- **Secrets**: administrator, migrator, runtime DB passwords and the JWT key are
  generated by Terraform and stored in Secrets Manager; they are never written
  to task definitions in clear (only `valueFrom` ARN references are).
```
