terraform {
  required_version = ">= 1.5"

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
  # By default this stack uses the LOCAL backend (terraform.tfstate on disk) so
  # that `terraform init` works out of the box without any pre-existing bucket.
  #
  # For any shared / team usage you MUST migrate the state to a remote S3 backend
  # with DynamoDB locking. Steps:
  #   1. Create the bucket + lock table once (manually or with a bootstrap stack):
  #        aws s3api create-bucket \
  #          --bucket spoony-prod-tfstate \
  #          --region eu-west-3 \
  #          --create-bucket-configuration LocationConstraint=eu-west-3
  #        aws s3api put-bucket-versioning \
  #          --bucket spoony-prod-tfstate \
  #          --versioning-configuration Status=Enabled
  #        aws dynamodb create-table \
  #          --table-name spoony-prod-tflock \
  #          --attribute-definitions AttributeName=LockID,AttributeType=S \
  #          --key-schema AttributeName=LockID,KeyType=HASH \
  #          --billing-mode PAY_PER_REQUEST \
  #          --region eu-west-3
  #   2. Uncomment the block below.
  #   3. Run `terraform init -migrate-state` to move the existing local state.
  #
  # backend "s3" {
  #   bucket         = "spoony-prod-tfstate"
  #   key            = "spoony/prod/terraform.tfstate"
  #   region         = "eu-west-3"
  #   dynamodb_table = "spoony-prod-tflock"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
