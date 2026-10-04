mock_provider "aws" {
  mock_data "aws_availability_zones" {
    defaults = {
      names = ["eu-west-3a", "eu-west-3b", "eu-west-3c"]
    }
  }

  mock_data "aws_ssm_parameter" {
    defaults = {
      value = "ami-0123456789abcdef0"
    }
  }

  mock_data "aws_partition" {
    defaults = {
      partition = "aws"
    }
  }

  mock_data "aws_iam_policy_document" {
    defaults = {
      json = "{\"Version\":\"2012-10-17\",\"Statement\":[]}"
    }
  }
}

mock_provider "random" {}

variables {
  aws_account_id       = "123456789012"
  api_domain           = "api.spoonrest.martincharrier.dev"
  acme_email           = "ops@example.test"
  cors_allowed_origins = "https://spoonrest.martincharrier.dev"
}

run "low_cost_arm64_plan" {
  command = plan

  assert {
    condition     = aws_instance.server.instance_type == "t4g.small"
    error_message = "The low-cost stack must default to t4g.small."
  }

  assert {
    condition     = aws_instance.server.credit_specification[0].cpu_credits == "standard"
    error_message = "CPU credits must stay in standard mode to avoid unlimited surplus charges."
  }

  assert {
    condition     = aws_instance.server.metadata_options[0].http_tokens == "required"
    error_message = "IMDSv2 must be mandatory."
  }

  assert {
    condition     = aws_instance.server.monitoring == false
    error_message = "Detailed EC2 monitoring is intentionally disabled for the low-cost beta."
  }

  assert {
    condition     = aws_ebs_volume.data.encrypted == true
    error_message = "The PostgreSQL data volume must be encrypted."
  }

  assert {
    condition     = toset([for rule in aws_security_group.server.ingress : rule.from_port]) == toset([80, 443])
    error_message = "Only Caddy HTTP and HTTPS ports may be exposed."
  }
}
