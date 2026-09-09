variable "region" {
  description = "AWS region to deploy into."
  type        = string
  default     = "eu-west-3"
}

variable "project_name" {
  description = "Project name, used as a prefix for resource names."
  type        = string
  default     = "spoony"
}

variable "environment" {
  description = "Deployment environment (used in the name prefix)."
  type        = string
  default     = "prod"
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
    condition     = var.desired_count >= 0
    error_message = "desired_count must be zero or greater."
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
}

variable "db_name" {
  description = "Initial PostgreSQL database name."
  type        = string
  default     = "spoony"
}

variable "db_username" {
  description = "RDS master username. For the V0 the app reuses the master user (see TODO in README)."
  type        = string
  default     = "spoony"
}

variable "cors_allowed_origins" {
  description = "Comma-separated list of origins allowed by the backend CORS config."
  type        = string
}

variable "acm_certificate_arn" {
  description = <<-EOT
    ACM certificate ARN (in this region) for HTTPS. It is mandatory when
    environment is prod. Non-production environments may explicitly use HTTP.
  EOT
  type        = string
  default     = ""
}

variable "github_repo" {
  description = "GitHub repo (owner/name) allowed to assume the deploy role via OIDC."
  type        = string
  default     = "MartinChrrr/Spoony-back"
}

variable "log_retention_days" {
  description = "CloudWatch Logs retention in days."
  type        = number
  default     = 30
}

variable "alarm_email" {
  description = "Optional operational email subscribed to CloudWatch alarms. The AWS confirmation email must be accepted."
  type        = string
  default     = ""
}

variable "jwt_access_expiration" {
  description = "JWT access token expiration in milliseconds (passed to the app)."
  type        = string
  default     = "900000"
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}
