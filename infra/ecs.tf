# -----------------------------------------------------------------------------
# ECS cluster, Fargate task definition and service.
# -----------------------------------------------------------------------------

resource "aws_ecs_cluster" "main" {
  name = "${local.name_prefix}-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
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
      name                   = "app"
      image                  = var.container_image
      essential              = true
      user                   = "spoony"
      readonlyRootFilesystem = true
      privileged             = false

      linuxParameters = {
        initProcessEnabled = true
        capabilities = {
          drop = ["ALL"]
        }
      }

      mountPoints = [
        {
          sourceVolume  = "tmp"
          containerPath = "/tmp"
          readOnly      = false
        }
      ]

      portMappings = [
        {
          containerPort = 8080
          protocol      = "tcp"
        }
      ]

      environment = [
        { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
        { name = "SPRING_FLYWAY_ENABLED", value = "false" },
        { name = "DATABASE_URL", value = local.database_jdbc_url },
        { name = "DATABASE_USER", value = var.db_app_username },
        { name = "CORS_ALLOWED_ORIGINS", value = var.cors_allowed_origins },
        { name = "JWT_ACCESS_EXPIRATION", value = var.jwt_access_expiration },
      ]

      secrets = [
        {
          name      = "DATABASE_PASSWORD"
          valueFrom = aws_secretsmanager_secret.db_app_password.arn
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
        # Liveness only detects a wedged process. It deliberately does not depend
        # on RDS, avoiding replacement loops during a database incident.
        command     = ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health/liveness || exit 1"]
        interval    = 30
        timeout     = 5
        retries     = 3
        startPeriod = 60
      }

      stopTimeout = 30
    }
  ])

  volume {
    name = "tmp"
  }

  tags = {
    Name = "${local.name_prefix}-app"
  }
}

# Flyway and role bootstrap run outside the web service. The workflow replaces
# the placeholder image, starts one task, waits for exit code 0, then deploys the
# long-running service. Admin/migrator secrets never enter the web container.
resource "aws_ecs_task_definition" "migration" {
  family                   = "${local.name_prefix}-migration"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "X86_64"
  }

  container_definitions = jsonencode([
    {
      name                   = "migration"
      image                  = var.container_image
      essential              = true
      user                   = "spoony"
      readonlyRootFilesystem = true
      privileged             = false

      entryPoint = ["java"]
      command = [
        "-cp",
        "app.jar",
        "-Dloader.main=com.spoony.backend.infrastructure.migration.DatabaseMigrationApplication",
        "org.springframework.boot.loader.launch.PropertiesLauncher",
      ]

      linuxParameters = {
        initProcessEnabled = true
        capabilities = {
          drop = ["ALL"]
        }
      }

      mountPoints = [
        {
          sourceVolume  = "tmp"
          containerPath = "/tmp"
          readOnly      = false
        }
      ]

      environment = [
        { name = "DATABASE_URL", value = local.database_jdbc_url },
        { name = "DATABASE_ADMIN_USER", value = var.db_username },
        { name = "DATABASE_MIGRATION_USER", value = var.db_migration_username },
        { name = "DATABASE_APP_USER", value = var.db_app_username },
      ]

      secrets = [
        {
          name      = "DATABASE_ADMIN_PASSWORD"
          valueFrom = aws_secretsmanager_secret.db_password.arn
        },
        {
          name      = "DATABASE_MIGRATION_PASSWORD"
          valueFrom = aws_secretsmanager_secret.db_migration_password.arn
        },
        {
          name      = "DATABASE_APP_PASSWORD"
          valueFrom = aws_secretsmanager_secret.db_app_password.arn
        },
      ]

      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
          "awslogs-region"        = var.region
          "awslogs-stream-prefix" = "migration"
        }
      }

      stopTimeout = 30
    }
  ])

  volume {
    name = "tmp"
  }

  tags = {
    Name = "${local.name_prefix}-migration"
  }
}

resource "aws_ecs_service" "app" {
  name                   = "${local.name_prefix}-svc"
  cluster                = aws_ecs_cluster.main.id
  task_definition        = aws_ecs_task_definition.app.arn
  desired_count          = var.desired_count
  launch_type            = "FARGATE"
  platform_version       = "LATEST"
  enable_execute_command = false

  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200

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

    precondition {
      condition = var.desired_count == 0 || (
        var.container_image != "" &&
        var.container_image != "public.ecr.aws/docker/library/busybox:latest"
      )
      error_message = "A running ECS service requires a real backend image; bootstrap with desired_count=0 first."
    }
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
