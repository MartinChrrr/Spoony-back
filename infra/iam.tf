# -----------------------------------------------------------------------------
# IAM: ECS execution role, ECS task role, GitHub OIDC deploy role.
# -----------------------------------------------------------------------------

data "aws_partition" "current" {}

# Trust policy shared by both ECS roles.
data "aws_iam_policy_document" "ecs_tasks_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

# ---------------------------------------------------------------------------
# (a) ECS task EXECUTION role: used by the ECS agent to pull the image, write
#     logs and inject secrets. NOT used by the app code itself.
# ---------------------------------------------------------------------------
resource "aws_iam_role" "ecs_execution" {
  name               = "${local.name_prefix}-ecs-execution"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json

  tags = {
    Name = "${local.name_prefix}-ecs-execution"
  }
}

# AWS-managed policy covering ECR pull + CloudWatch Logs create/put.
resource "aws_iam_role_policy_attachment" "ecs_execution_managed" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Inline policy: allow reading exactly the two secrets injected into the task,
# and writing to the task's log group.
data "aws_iam_policy_document" "ecs_execution_extra" {
  statement {
    sid    = "ReadTaskSecrets"
    effect = "Allow"
    actions = [
      "secretsmanager:GetSecretValue",
    ]
    resources = [
      aws_secretsmanager_secret.db_password.arn,
      aws_secretsmanager_secret.jwt_secret.arn,
    ]
  }

  statement {
    sid    = "WriteTaskLogs"
    effect = "Allow"
    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
    ]
    resources = [
      "${aws_cloudwatch_log_group.ecs.arn}:*",
    ]
  }
}

resource "aws_iam_role_policy" "ecs_execution_extra" {
  name   = "${local.name_prefix}-ecs-execution-extra"
  role   = aws_iam_role.ecs_execution.id
  policy = data.aws_iam_policy_document.ecs_execution_extra.json
}

# ---------------------------------------------------------------------------
# (b) ECS task ROLE: the identity the running app assumes. Minimal for the V0
#     (no AWS API calls from the app yet); attach policies here when needed.
# ---------------------------------------------------------------------------
resource "aws_iam_role" "ecs_task" {
  name               = "${local.name_prefix}-ecs-task"
  assume_role_policy = data.aws_iam_policy_document.ecs_tasks_assume.json

  tags = {
    Name = "${local.name_prefix}-ecs-task"
  }
}

# ---------------------------------------------------------------------------
# (c) GitHub OIDC provider + deploy role.
# ---------------------------------------------------------------------------
resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  # GitHub's OIDC certificate thumbprint (well-known value). AWS now validates the
  # token against its trusted-CA library, so this field is kept only because the
  # API still requires it; refresh it if GitHub ever rotates its CA.
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]

  tags = {
    Name = "${local.name_prefix}-github-oidc"
  }
}

# Trust: only the configured repo, on the main branch OR the "production"
# GitHub Environment, may assume the role.
data "aws_iam_policy_document" "github_deploy_assume" {
  statement {
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.github.arn]
    }

    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:aud"
      values   = ["sts.amazonaws.com"]
    }

    # Exact match (no wildcard in either value), so StringEquals is correct and
    # tighter than StringLike.
    condition {
      test     = "StringEquals"
      variable = "token.actions.githubusercontent.com:sub"
      values = [
        "repo:${var.github_repo}:ref:refs/heads/main",
        "repo:${var.github_repo}:environment:production",
      ]
    }
  }
}

resource "aws_iam_role" "github_deploy" {
  name               = "${local.name_prefix}-github-deploy"
  assume_role_policy = data.aws_iam_policy_document.github_deploy_assume.json

  tags = {
    Name = "${local.name_prefix}-github-deploy"
  }
}

# Deploy permissions: push to ECR, register a new task def, update the service,
# and pass exactly the two ECS roles to the new task definition.
data "aws_iam_policy_document" "github_deploy" {
  statement {
    sid       = "EcrAuth"
    effect    = "Allow"
    actions   = ["ecr:GetAuthorizationToken"]
    resources = ["*"]
  }

  statement {
    sid    = "EcrPush"
    effect = "Allow"
    actions = [
      "ecr:BatchCheckLayerAvailability",
      "ecr:CompleteLayerUpload",
      "ecr:InitiateLayerUpload",
      "ecr:PutImage",
      "ecr:UploadLayerPart",
      "ecr:BatchGetImage",
      "ecr:GetDownloadUrlForLayer",
    ]
    resources = [aws_ecr_repository.backend.arn]
  }

  # Register/Describe task definitions: these actions do not support
  # resource-level permissions, so "*" is required by AWS.
  statement {
    sid    = "EcsTaskDef"
    effect = "Allow"
    actions = [
      "ecs:DescribeTaskDefinition",
      "ecs:RegisterTaskDefinition",
    ]
    resources = ["*"]
  }

  # Update/Describe the service: scoped to exactly this service's ARN.
  statement {
    sid    = "EcsUpdateService"
    effect = "Allow"
    actions = [
      "ecs:DescribeServices",
      "ecs:UpdateService",
    ]
    resources = [aws_ecs_service.app.id]
  }

  statement {
    sid    = "PassEcsRoles"
    effect = "Allow"
    actions = ["iam:PassRole"]
    resources = [
      aws_iam_role.ecs_execution.arn,
      aws_iam_role.ecs_task.arn,
    ]
    condition {
      test     = "StringEquals"
      variable = "iam:PassedToService"
      values   = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy" "github_deploy" {
  name   = "${local.name_prefix}-github-deploy"
  role   = aws_iam_role.github_deploy.id
  policy = data.aws_iam_policy_document.github_deploy.json
}
