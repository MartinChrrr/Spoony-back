terraform {
  required_version = ">= 1.10"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.6"
    }
  }

  # ---------------------------------------------------------------------------
  # Backend
  # ---------------------------------------------------------------------------
  # Production state is remote by construction. The account-specific bucket is
  # supplied at init time (`-backend-config=backend-prod.hcl`) and is never
  # hard-coded in the repository. S3 native lockfiles replace DynamoDB locking.
  backend "s3" {
    key          = "spoony/prod/terraform.tfstate"
    region       = "eu-west-3"
    encrypt      = true
    use_lockfile = true
  }
}

provider "aws" {
  region              = var.region
  allowed_account_ids = [var.aws_account_id]

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
