# -----------------------------------------------------------------------------
# ECS cluster, Fargate task definition and service.
# -----------------------------------------------------------------------------

resource "aws_ecs_cluster" "main" {
  name = "${local.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "disabled" # cost-optimised V0; enable for richer metrics later.
  }

  tags = {
    Name = "${local.name_prefix}-cluster"
  }
}

locals {
  # JDBC URL built from the RDS host (.address is the bare hostname, without the
  # ":port" that .endpoint carries) plus the standard 5432 and sslmode=require to
  # match the rds.force_ssl=1 server setting and the app's DATABASE_URL contract.
  db_host           = aws_db_instance.main.address
  database_jdbc_url = "jdbc:postgresql://${local.db_host}:5432/${var.db_name}?sslmode=require"
}

resource "aws_ecs_task_definition" "app" {
  family                   = "${local.name_prefix}-app"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.container_cpu
  memory                   = var.container_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([
    {
      name      = "app"
      image     = var.container_image
      essential = true

      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "DATABASE_URL", value = local.database_jdbc_url },
        { name = "DATABASE_USER", value = var.db_username },
        { name = "CORS_ALLOWED_ORIGINS", value = var.cors_allowed_origins },
        { name = "JWT_ACCESS_EXPIRATION", value = var.jwt_access_expiration },
      ]

      secrets = [
        {
          name      = "DATABASE_PASSWORD"
          valueFrom = aws_secretsmanager_secret.db_password.arn
        },
        {
          name      = "JWT_SECRET"
          valueFrom = aws_secretsmanager_secret.jwt_secret.arn
        },
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "app"
        }
      }

      healthCheck = {
        command     = ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }
    }
  ])

  tags = {
    Name = "${local.name_prefix}-app"
  }
}

resource "aws_ecs_service" "app" {
  name            = "${local.name_prefix}-svc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "app"
    container_port   = 8080
  }

  health_check_grace_period_seconds = 120

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  # The CD pipeline registers new task definition revisions and may scale the
  # service; Terraform must not revert those out-of-band changes.
  lifecycle {
    ignore_changes = [task_definition, desired_count]
  }

  # A listener must exist before the service can attach to the target group.
  # Listeners are conditional (count), so depend on all of them — empty lists
  # are valid in depends_on and resolve to whichever set actually exists.
  depends_on = [
    aws_lb_listener.http_forward,
    aws_lb_listener.http_redirect,
    aws_lb_listener.https,
  ]

  tags = {
    Name = "${local.name_prefix}-svc"
  }
}
