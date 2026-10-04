output "public_ip" {
  description = "Elastic IP to use in the Cloudflare A record."
  value       = aws_eip.server.public_ip
}

output "instance_id" {
  description = "EC2 instance targeted by Systems Manager."
  value       = aws_instance.server.id
}

output "artifact_bucket" {
  description = "Private S3 bucket containing releases and database dumps."
  value       = aws_s3_bucket.artifacts.id
}

output "github_deploy_role_arn" {
  description = "AWS role used by GitHub Actions via OIDC."
  value       = aws_iam_role.github_deploy.arn
}

output "api_url" {
  description = "Expected public API URL after the Cloudflare record exists."
  value       = "https://${var.api_domain}"
}

output "data_volume_id" {
  description = "Protected EBS volume containing PostgreSQL data."
  value       = aws_ebs_volume.data.id
}

output "ssm_parameter_prefix" {
  description = "Prefix containing encrypted runtime parameters."
  value       = local.parameter_prefix
}
