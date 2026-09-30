variable "region" {
  description = "AWS region. Production health data is pinned to Paris."
  type        = string
  default     = "eu-west-3"

  validation {
    condition     = var.environment != "prod" || var.region == "eu-west-3"
    error_message = "Production must stay in eu-west-3."
  }
}

variable "aws_account_id" {
  description = "Expected 12-digit AWS account ID."
  type        = string

  validation {
    condition     = can(regex("^[0-9]{12}$", var.aws_account_id))
    error_message = "aws_account_id must contain exactly 12 digits."
  }
}

variable "project_name" {
  description = "Resource name prefix."
  type        = string
  default     = "spoony"

  validation {
    condition     = can(regex("^[a-z][a-z0-9-]{1,20}$", var.project_name))
    error_message = "project_name must be 2-21 lowercase letters, digits or hyphens."
  }
}

variable "environment" {
  description = "Deployment environment."
  type        = string
  default     = "prod"

  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "environment must be dev, staging or prod."
  }
}

variable "api_domain" {
  description = "Public API hostname managed in Cloudflare."
  type        = string
  default     = "api.spoonrest.martincharrier.dev"

  validation {
    condition     = can(regex("^[a-z0-9.-]+\\.[a-z]{2,}$", var.api_domain))
    error_message = "api_domain must be a hostname without scheme or path."
  }
}

variable "acme_email" {
  description = "Email used by Caddy for ACME certificate notices."
  type        = string

  validation {
    condition     = can(regex("^[^@'[:space:]]+@[^@'[:space:]]+\\.[^@'[:space:]]+$", var.acme_email))
    error_message = "acme_email must be a valid email address."
  }
}

variable "cors_allowed_origins" {
  description = "Comma-separated HTTPS origins allowed by Spring CORS."
  type        = string

  validation {
    condition = (
      var.cors_allowed_origins != "*" &&
      !strcontains(var.cors_allowed_origins, "'") &&
      !strcontains(var.cors_allowed_origins, "\n") &&
      can(regex("^https://[^, ]+(,https://[^, ]+)*$", var.cors_allowed_origins))
    )
    error_message = "Use an HTTPS comma-separated origin list without wildcard or spaces."
  }
}

variable "instance_type" {
  description = "ARM64 burstable instance used by the beta."
  type        = string
  default     = "t4g.small"

  validation {
    condition     = startswith(var.instance_type, "t4g.")
    error_message = "This stack is ARM64 and only accepts t4g instance types."
  }
}

variable "root_volume_size_gib" {
  description = "Encrypted EC2 root volume size."
  type        = number
  default     = 10

  validation {
    condition     = var.root_volume_size_gib >= 8 && var.root_volume_size_gib <= 30
    error_message = "root_volume_size_gib must be between 8 and 30."
  }
}

variable "data_volume_size_gib" {
  description = "Encrypted persistent volume holding PostgreSQL and Caddy data."
  type        = number
  default     = 20

  validation {
    condition     = var.data_volume_size_gib >= 10 && var.data_volume_size_gib <= 100
    error_message = "data_volume_size_gib must be between 10 and 100."
  }
}

variable "backup_retention_days" {
  description = "Number of days before database dumps expire from S3."
  type        = number
  default     = 35

  validation {
    condition     = var.backup_retention_days >= 7 && var.backup_retention_days <= 365
    error_message = "backup_retention_days must be between 7 and 365."
  }
}

variable "release_retention_days" {
  description = "Number of days before deployment bundles expire from S3."
  type        = number
  default     = 7

  validation {
    condition     = var.release_retention_days >= 2 && var.release_retention_days <= 30
    error_message = "release_retention_days must be between 2 and 30."
  }
}

variable "github_repo" {
  description = "GitHub owner/repository allowed to deploy through OIDC."
  type        = string
  default     = "MartinChrrr/Spoony-back"

  validation {
    condition     = can(regex("^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$", var.github_repo))
    error_message = "github_repo must use owner/repository format."
  }
}

variable "github_oidc_provider_arn" {
  description = "Existing GitHub OIDC provider ARN. Leave empty to create it."
  type        = string
  default     = ""

  validation {
    condition = var.github_oidc_provider_arn == "" || can(regex(
      "^arn:[^:]+:iam::${var.aws_account_id}:oidc-provider/token\\.actions\\.githubusercontent\\.com$",
      var.github_oidc_provider_arn
    ))
    error_message = "github_oidc_provider_arn must reference GitHub in aws_account_id."
  }
}

variable "vpc_cidr" {
  description = "Dedicated VPC CIDR."
  type        = string
  default     = "10.30.0.0/16"
}

variable "public_subnet_cidr" {
  description = "Public subnet CIDR for the single beta instance."
  type        = string
  default     = "10.30.1.0/24"
}

locals {
  name_prefix      = "${var.project_name}-${var.environment}"
  parameter_prefix = "/${var.project_name}/${var.environment}"
  artifact_bucket  = "${local.name_prefix}-artifacts-${var.aws_account_id}-${var.region}"
}
