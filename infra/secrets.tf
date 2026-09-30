# -----------------------------------------------------------------------------
# Generated administrator/migrator/runtime DB passwords and JWT signing key,
# stored in Secrets Manager.
#
# TODO post-V0: enable automatic rotation.
#   - DB password: use a Secrets Manager rotation Lambda (the AWS-provided
#     PostgreSQL single-user rotation function) wired to this secret, and update
#     the RDS master password through it rather than via Terraform.
#   - JWT secret: rotating a signing key invalidates live tokens; plan a
#     dual-key (kid) strategy in the app before enabling rotation.
# -----------------------------------------------------------------------------

# DB password: 32 chars, no ambiguous/JDBC-URL-breaking characters
# (override_special restricts the special set so the password is safe in a
# jdbc:postgresql URL and in env vars).
resource "random_password" "db" {
  length           = 32
  special          = true
  override_special = "!#%*-_=+"
  min_lower        = 4
  min_upper        = 4
  min_numeric      = 4
}

resource "random_password" "db_migration" {
  length           = 32
  special          = true
  override_special = "!#%*-_=+"
  min_lower        = 4
  min_upper        = 4
  min_numeric      = 4
}

resource "random_password" "db_app" {
  length           = 32
  special          = true
  override_special = "!#%*-_=+"
  min_lower        = 4
  min_upper        = 4
  min_numeric      = 4
}

# JWT signing secret: 48 chars.
resource "random_password" "jwt" {
  length           = 48
  special          = true
  override_special = "!#%*-_=+"
  min_lower        = 4
  min_upper        = 4
  min_numeric      = 4
}

resource "aws_secretsmanager_secret" "db_password" {
  name        = "${local.name_prefix}/db-password"
  description = "RDS master password for ${local.name_prefix}."

  tags = {
    Name = "${local.name_prefix}-db-password"
  }
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = random_password.db.result
}

resource "aws_secretsmanager_secret" "db_migration_password" {
  name        = "${local.name_prefix}/db-migration-password"
  description = "One-shot Flyway role password for ${local.name_prefix}."

  tags = {
    Name = "${local.name_prefix}-db-migration-password"
  }
}

resource "aws_secretsmanager_secret_version" "db_migration_password" {
  secret_id     = aws_secretsmanager_secret.db_migration_password.id
  secret_string = random_password.db_migration.result
}

resource "aws_secretsmanager_secret" "db_app_password" {
  name        = "${local.name_prefix}/db-app-password"
  description = "Least-privilege runtime database password for ${local.name_prefix}."

  tags = {
    Name = "${local.name_prefix}-db-app-password"
  }
}

resource "aws_secretsmanager_secret_version" "db_app_password" {
  secret_id     = aws_secretsmanager_secret.db_app_password.id
  secret_string = random_password.db_app.result
}

resource "aws_secretsmanager_secret" "jwt_secret" {
  name        = "${local.name_prefix}/jwt-secret"
  description = "JWT signing secret for ${local.name_prefix}."

  tags = {
    Name = "${local.name_prefix}-jwt-secret"
  }
}

resource "aws_secretsmanager_secret_version" "jwt_secret" {
  secret_id     = aws_secretsmanager_secret.jwt_secret.id
  secret_string = random_password.jwt.result
}
