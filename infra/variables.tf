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
    definition. The default is a tiny public placeholder so the very first
    `terraform apply` can register the task definition (ECS rejects an empty
    image). With the placeholder the service starts a task but stays UNHEALTHY
    on /actuator/health (busybox does not listen on 8080) until the CD pipeline
    pushes the first real image; Terraform then ignores task_definition changes
    (see ecs.tf lifecycle), so the pipeline owns the image from then on.
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
  description = "Number of ECS tasks to run. Kept at 1 for a cost-optimised V0."
  type        = number
  default     = 1
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
    ACM certificate ARN (in this region) for HTTPS. If empty, only an HTTP:80
    listener is created (plain text, NOT acceptable for real prod). If set, an
    HTTPS:443 listener is created and HTTP:80 redirects to it.
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

variable "jwt_access_expiration" {
  description = "JWT access token expiration in milliseconds (passed to the app)."
  type        = string
  default     = "900000"
}

locals {
  name_prefix = "${var.project_name}-${var.environment}"
}
