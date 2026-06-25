# -----------------------------------------------------------------------------
# RDS PostgreSQL 16 (single-AZ, encrypted, in the private subnets).
# -----------------------------------------------------------------------------

resource "aws_db_subnet_group" "main" {
  name       = "${local.name_prefix}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${local.name_prefix}-db-subnet-group"
  }
}

# Parameter group enforcing SSL connections (sslmode=require on the client side
# is paired with rds.force_ssl=1 on the server side).
resource "aws_db_parameter_group" "main" {
  name        = "${local.name_prefix}-pg16"
  family      = "postgres16"
  description = "PostgreSQL 16 parameters for ${local.name_prefix} (force SSL)."

  parameter {
    name  = "rds.force_ssl"
    value = "1"
  }

  tags = {
    Name = "${local.name_prefix}-pg16"
  }
}

resource "aws_db_instance" "main" {
  identifier     = "${local.name_prefix}-db"
  engine         = "postgres"
  engine_version = "16"
  instance_class = var.db_instance_class

  allocated_storage = var.db_allocated_storage
  storage_type      = "gp3"
  storage_encrypted = true

  db_name  = var.db_name
  username = var.db_username
  password = random_password.db.result
  port     = 5432

  db_subnet_group_name   = aws_db_subnet_group.main.name
  parameter_group_name   = aws_db_parameter_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = false

  backup_retention_period    = 7
  auto_minor_version_upgrade = true
  deletion_protection        = true
  skip_final_snapshot        = false
  final_snapshot_identifier  = "${local.name_prefix}-db-final-${formatdate("YYYYMMDDhhmmss", timestamp())}"

  # The final_snapshot_identifier embeds a timestamp() that changes on every plan;
  # it is only consumed at delete time, so ignore drift to avoid spurious diffs.
  lifecycle {
    ignore_changes = [final_snapshot_identifier]
  }

  tags = {
    Name = "${local.name_prefix}-db"
  }
}
