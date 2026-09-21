resource "random_password" "db_admin" {
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

resource "random_password" "jwt" {
  length           = 64
  special          = true
  override_special = "!#%*-_=+"
  min_lower        = 8
  min_upper        = 8
  min_numeric      = 8
}

locals {
  secret_parameters = {
    db-admin-password     = random_password.db_admin.result
    db-migration-password = random_password.db_migration.result
    db-app-password       = random_password.db_app.result
    jwt-secret            = random_password.jwt.result
  }
}

resource "aws_ssm_parameter" "secret" {
  for_each = local.secret_parameters

  name        = "${local.parameter_prefix}/${each.key}"
  description = "Generated ${each.key} for ${local.name_prefix}."
  type        = "SecureString"
  value       = each.value

  tags = { Name = "${local.name_prefix}-${each.key}" }
}
