data "aws_ssm_parameter" "al2023_arm64" {
  name = "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-arm64"
}

resource "aws_instance" "server" {
  ami                                  = nonsensitive(data.aws_ssm_parameter.al2023_arm64.value)
  instance_type                        = var.instance_type
  subnet_id                            = aws_subnet.public.id
  vpc_security_group_ids               = [aws_security_group.server.id]
  iam_instance_profile                 = aws_iam_instance_profile.server.name
  associate_public_ip_address          = false
  monitoring                           = false
  instance_initiated_shutdown_behavior = "stop"

  user_data = templatefile("${path.module}/templates/cloud-init.sh.tftpl", {
    data_volume_device_id = replace(aws_ebs_volume.data.id, "-", "")
    compose_version       = "v2.39.4"
    compose_sha256        = "49082844b87f03cdcd5f5bbef1ba8c9c897b7a2dfb80cea18d61ec8ca6117e0c"
  })

  user_data_replace_on_change = false

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
    instance_metadata_tags      = "disabled"
  }

  root_block_device {
    encrypted             = true
    volume_type           = "gp3"
    volume_size           = var.root_volume_size_gib
    delete_on_termination = true
  }

  credit_specification {
    cpu_credits = "standard"
  }

  # The AWS provider reports the separately managed Elastic IP as an
  # auto-assigned public address after refresh, and folds the separately
  # attached data disk into ebs_block_device/volume_tags. Those computed
  # values would otherwise create a perpetual diff and replace the instance.
  # The EIP and persistent disk remain managed by aws_eip.server,
  # aws_ebs_volume.data and aws_volume_attachment.data below.
  lifecycle {
    ignore_changes = [
      associate_public_ip_address,
      ebs_block_device,
      volume_tags,
    ]
  }

  volume_tags = {
    Name      = "${local.name_prefix}-root"
    DataClass = "system"
  }

  tags = { Name = "${local.name_prefix}-server" }

  depends_on = [aws_iam_role_policy_attachment.ssm]
}

resource "aws_volume_attachment" "data" {
  device_name = "/dev/sdf"
  volume_id   = aws_ebs_volume.data.id
  instance_id = aws_instance.server.id
}

resource "aws_eip" "server" {
  domain   = "vpc"
  instance = aws_instance.server.id

  tags = { Name = "${local.name_prefix}-public-ip" }

  depends_on = [aws_internet_gateway.main]
}
