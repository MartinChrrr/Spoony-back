# Spoony backend — AWS infrastructure (Terraform) & deployment runbook

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
- `aws-cli` v2, configured (`aws configure`) for **eu-west-3**.
- `docker` (to build/push the first image, if not using the pipeline).
- A domain and an **ACM certificate in eu-west-3** for production; Terraform
  rejects `environment="prod"` when `acm_certificate_arn` is empty.
- A versioned, encrypted, private S3 bucket for Terraform state.

## Files

| File | Purpose |
|------|---------|
| `versions.tf` | Terraform/provider versions and mandatory S3 backend with native lockfile. |
| `variables.tf` | Inputs + `local.name_prefix = "${project_name}-${environment}"`. |
| `terraform.tfvars.example` | Sample values — copy to `terraform.tfvars`. |
| `network.tf` | VPC, IGW, 2 public + 2 private subnets, routes, 3 security groups. |
| `ecr.tf` | ECR repo (immutable, scan-on-push, keep last 10 images). |
| `secrets.tf` | Generated DB password + JWT secret in Secrets Manager. |
| `rds.tf` | RDS PostgreSQL 16, encrypted, force-SSL parameter group. |
| `logs.tf` | CloudWatch log group `/ecs/<prefix>`. |
| `monitoring.tf` | SNS topic and core ALB/ECS/RDS/certificate alarms. |
| `iam.tf` | ECS execution/task roles, GitHub OIDC provider + deploy role. |
| `alb.tf` | ALB, target group, conditional HTTP/HTTPS listeners. |
| `ecs.tf` | Cluster, task definition, service. |
| `outputs.tf` | Values consumed by the pipeline and this runbook. |

## Deployment order

### 1. Initialise Terraform

```bash
cd infra
cp backend-prod.hcl.example backend-prod.hcl
# edit backend-prod.hcl with the pre-created state bucket
terraform init -backend-config=backend-prod.hcl
```

The bucket bootstrap commands are in `DEPLOY.md`. State is encrypted and
versioned in S3, with Terraform's native `use_lockfile` locking. Local state is
not an accepted production mode.

### 2. Apply

```bash
cp terraform.tfvars.example terraform.tfvars
# edit terraform.tfvars: cors_allowed_origins (required), acm_certificate_arn, etc.
terraform apply
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

### 3. Push a first image

Either run the GitHub **Deploy** workflow (manually via *Run workflow*, once the
repo secrets below are set), **or** build & push manually:

```bash
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REGION=eu-west-3
REPO_URL=$(terraform output -raw ecr_repository_url)

aws ecr get-login-password --region "$REGION" \
  | docker login --username AWS --password-stdin "${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"

# build from the repo root (Dockerfile is there)
docker build -t "${REPO_URL}:bootstrap" ..
docker push "${REPO_URL}:bootstrap"
```

If you push manually, you must **register a new task-definition revision** that
points at the pushed image and then update the service. (`--force-new-deployment`
alone just re-runs the *same* placeholder revision and will not pick up the new
image.)

```bash
CLUSTER=$(terraform output -raw ecs_cluster_name)
SERVICE=$(terraform output -raw ecs_service_name)
FAMILY=$(terraform output -raw ecs_task_family)

# take the current task def, swap the image, register a new revision
aws ecs describe-task-definition --task-definition "$FAMILY" \
  --query 'taskDefinition' --output json \
  | jq --arg IMG "${REPO_URL}:bootstrap" \
      'del(.taskDefinitionArn,.revision,.status,.requiresAttributes,.compatibilities,.registeredAt,.registeredBy) | .containerDefinitions[0].image=$IMG' \
  > /tmp/td.json
NEW_TD=$(aws ecs register-task-definition --cli-input-json file:///tmp/td.json \
  --query 'taskDefinition.taskDefinitionArn' --output text)

aws ecs update-service --cluster "$CLUSTER" --service "$SERVICE" \
  --task-definition "$NEW_TD" --desired-count 1 --region "$REGION"
```

> In practice the simplest path is to set the GitHub secrets (step below) and
> let the **Deploy** workflow build, scan, push, register and roll out the first
> image automatically.

### 4. Wait for stability

ECS deploys the task; the ALB target becomes healthy once
`/actuator/health/readiness` returns 200 (allow ~1–2 min for cold start + Flyway).

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

- **Dedicated least-privilege DB user.** For the V0 the app uses the RDS master
  user. Create a non-superuser role with only the privileges it needs on the
  `spoony` schema, store its credentials in a separate secret, and point
  `DATABASE_USER` / `DATABASE_PASSWORD` at it.
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
- **Secrets**: DB password and JWT key are generated by Terraform and stored in
  Secrets Manager; they are never written to the task definition in clear (only
  `valueFrom` ARN references are).
```
