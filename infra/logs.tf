# -----------------------------------------------------------------------------
# CloudWatch Logs group for the ECS task (awslogs driver writes here).
# -----------------------------------------------------------------------------

resource "aws_cloudwatch_log_group" "ecs" {
  name              = "/ecs/${local.name_prefix}"
  retention_in_days = var.log_retention_days

  tags = {
    Name = "/ecs/${local.name_prefix}"
  }
}
