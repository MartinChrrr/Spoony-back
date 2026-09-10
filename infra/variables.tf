variable "region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "eu-west-3"

  validation {
    condition     = var.environment != "prod" || var.region == "eu-west-3"
    error_message = "Production is pinned to eu-west-3 so health data cannot be deployed outside the chosen EU region."
  }
}

variable "aws_account_id" {
  description = "Expected 12-digit AWS account ID. The provider refuses to operate against any other account."
  type        = string

  validation {
    condition     = can(regex("^[0-9]{12}$", var.aws_account_id))
    error_message = "aws_account_id must contain exactly 12 digits."
  }
}

variable "project_name" {
  description = "Project name, used as a prefix for resource names."
  type        = string
  default     = "spoony"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{1,20}$", var.project_name))
    error_message = "project_name must be 2-21 lowercase letters, digits or hyphens, starting with a letter."
  }
}

variable "environment" {
  description = "Deployment environment (used in the name prefix)."
  type        = string
  default     = "prod"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "environment must be one of: dev, staging, prod."
  }
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "container_image" {
  description = <<-EOT
    Full ECR image reference (repo:tag or repo@digest) deployed by the task
    definition. The placeholder may only be used while desired_count is zero.
    Before starting the service, push a real immutable image to ECR and set its
    full tag or digest here.
  EOT
  type        = string
  default     = "public.ecr.aws/docker/library/busybox:latest"
}

variable "container_cpu" {
  description = "Fargate task CPU units (512 = 0.5 vCPU)."
  type        = number
  default     = 512
}

variable "container_memory" {
  description = "Fargate task memory in MiB."
  type        = number
  default     = 1024
}

variable "desired_count" {
  description = "Number of ECS tasks to run. Zero is the safe bootstrap default; use one only for a controlled beta."
  type        = number
  default     = 0

  validation {
    condition     = var.desired_count >= 0 && var.desired_count <= 10 && floor(var.desired_count) == var.desired_count
    error_message = "desired_count must be an integer between 0 and 10."
  }
}

variable "db_instance_class" {
  description = "RDS instance class."
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GiB."
  type        = number
  default     = 20

  validation {
    condition     = var.db_allocated_storage >= 20 && var.db_allocated_storage <= 1000
    error_message = "db_allocated_storage must be between 20 and 1000 GiB."
  }
}

variable "db_name" {
  description = "Initial PostgreSQL database name."
  type        = string
  default     = "spoony"
}

variable "db_username" {
  description = "RDS master username, used only by the one-shot database bootstrap task."
  type        = string
  default     = "spoony"

  validation {
    condition     = can(regex("^[a-zA-Z][a-zA-Z0-9_]{0,62}$", var.db_username))
    error_message = "db_username must be a valid PostgreSQL identifier of at most 63 characters."
  }
}

variable "db_migration_username" {
  description = "PostgreSQL role used only by the one-shot Flyway ECS task."
  type        = string
  default     = "spoony_migrator"

  validation {
    condition     = can(regex("^[a-zA-Z][a-zA-Z0-9_]{0,62}$", var.db_migration_username))
    error_message = "db_migration_username must be a valid PostgreSQL identifier of at most 63 characters."
  }
}

variable "db_app_username" {
  description = "Least-privilege PostgreSQL role used by the long-running web service."
  type        = string
  default     = "spoony_app"

  validation {
    condition     = can(regex("^[a-zA-Z][a-zA-Z0-9_]{0,62}$", var.db_app_username))
    error_message = "db_app_username must be a valid PostgreSQL identifier of at most 63 characters."
  }
}

check "database_roles_are_distinct" {
  assert {
    condition = length(toset([
      var.db_username,
      var.db_migration_username,
      var.db_app_username,
    ])) == 3
    error_message = "db_username, db_migration_username and db_app_username must be three distinct PostgreSQL roles."
  }
}

variable "cors_allowed_origins" {
  description = "Comma-separated list of origins allowed by the backend CORS config."
  type        = string

  validation {
    condition = var.environment != "prod" || (
      var.cors_allowed_origins != "*" &&
      can(regex("^https://[^, ]+(,https://[^, ]+)*$", var.cors_allowed_origins))
    )
    error_message = "Production CORS origins must be an HTTPS comma-separated list without spaces; wildcard is forbidden."
  }
}

variable "acm_certificate_arn" {
  description = <<-EOT
    ACM certificate ARN (in this region) for HTTPS. It is mandatory when
    environment is prod. Non-production environments may explicitly use HTTP.
  EOT
  type        = string
  default     = ""

  validation {
    condition = var.acm_certificate_arn == "" || can(regex(
      "^arn:[^:]+:acm:${var.region}:[0-9]{12}:certificate/.+$",
      var.acm_certificate_arn
    ))
    error_message = "acm_certificate_arn must be an ACM certificate ARN in the configured region."
  }
}

variable "github_repo" {
  description = "GitHub repo (owner/name) allowed to assume the deploy role via OIDC."
  type        = string
  default     = "MartinChrrr/Spoony-back"
}

variable "github_oidc_provider_arn" {
  description = "Existing GitHub Actions OIDC provider ARN for this AWS account. Leave empty to create it with this stack."
  type        = string
  default     = ""

  validation {
    condition = var.github_oidc_provider_arn == "" || can(regex(
      "^arn:[^:]+:iam::${var.aws_account_id}:oidc-provider/token\\.actions\\.githubusercontent\\.com$",
      var.github_oidc_provider_arn
    ))
    error_message = "github_oidc_provider_arn must reference token.actions.githubusercontent.com in aws_account_id."
  }
}

variable "log_retention_days" {
  description = "CloudWatch Logs retention in days."
  type        = number
  default     = 30

  validation {
    condition = contains([
      1, 3, 5, 7, 14, 30, 60, 90, 120, 150, 180, 365, 400, 545,
      731, 1096, 1827, 2192, 2557, 2922, 3288, 3653
    ], var.log_retention_days)
    error_message = "log_retention_days must be a retention value supported by CloudWatch Logs."
  }
}

variable "alarm_email" {
  description = "Optional operational email subscribed to CloudWatch alarms. The AWS confirmation email must be accepted."
  type        = string
  default     = ""

  validation {
    condition     = var.alarm_email == "" || can(regex("^[^@[:space:]]+@[^@[:space:]]+\\.[^@[:space:]]+$", var.alarm_email))
    error_message = "alarm_email must be empty or a valid email address."
  }
}

variable "jwt_access_expiration" {
  description = "JWT access token expiration in milliseconds (passed to the app)."
  type        = string
  default     = "900000"

  validation {
    condition     = can(tonumber(var.jwt_access_expiration)) && tonumber(var.jwt_access_expiration) >= 60000
    error_message = "jwt_access_expiration must be a numeric string of at least 60000 milliseconds."
  }
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}
