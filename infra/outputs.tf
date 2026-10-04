# -----------------------------------------------------------------------------
# Outputs consumed by the CD pipeline and the runbook.
# -----------------------------------------------------------------------------

output "alb_dns_name" {
  description = "Public DNS name of the ALB (entry point for the API)."
  value       = aws_lb.main.dns_name
}

output "ecr_repository_url" {
  description = "ECR repository URL to push images to."
  value       = aws_ecr_repository.backend.repository_url
}

output "ecr_repository_name" {
  description = "ECR repository name (CD secret ECR_REPOSITORY, bare name)."
  value       = aws_ecr_repository.backend.name
}

output "ecs_cluster_name" {
  description = "ECS cluster name (CD secret ECS_CLUSTER)."
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "ECS service name (CD secret ECS_SERVICE)."
  value       = aws_ecs_service.app.name
}

output "ecs_task_family" {
  description = "ECS task definition family (used by the pipeline to fetch the current task def)."
  value       = aws_ecs_task_definition.app.family
}

output "ecs_migration_task_family" {
  description = "One-shot database bootstrap/Flyway task family used by the CD pipeline."
  value       = aws_ecs_task_definition.migration.family
}

output "task_execution_role_arn" {
  description = "ECS task execution role ARN."
  value       = aws_iam_role.ecs_execution.arn
}

output "task_role_arn" {
  description = "ECS task role ARN."
  value       = aws_iam_role.ecs_task.arn
}

output "github_deploy_role_arn" {
  description = "ARN of the GitHub OIDC deploy role (CD secret AWS_DEPLOY_ROLE_ARN)."
  value       = aws_iam_role.github_deploy.arn
}

output "db_secret_arn" {
  description = "Secrets Manager ARN of the least-privilege runtime DB password."
  value       = aws_secretsmanager_secret.db_app_password.arn
}

output "db_admin_secret_arn" {
  description = "Secrets Manager ARN of the RDS administrator password, used only by the migration task."
  value       = aws_secretsmanager_secret.db_password.arn
}

output "db_migration_secret_arn" {
  description = "Secrets Manager ARN of the Flyway migration role password."
  value       = aws_secretsmanager_secret.db_migration_password.arn
}

output "jwt_secret_arn" {
  description = "Secrets Manager ARN of the JWT secret."
  value       = aws_secretsmanager_secret.jwt_secret.arn
}

output "rds_endpoint" {
  description = "RDS endpoint (host:port)."
  value       = aws_db_instance.main.endpoint
  sensitive   = true
}

output "alarm_topic_arn" {
  description = "SNS topic receiving all production alarms."
  value       = aws_sns_topic.alarms.arn
}
