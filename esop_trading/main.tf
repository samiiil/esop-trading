terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  region = "us-east-1"
}

resource "tls_private_key" "rsa" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

resource "aws_key_pair" "samiksha-gurukul" {
  key_name   = "samiksha-gurukul"
  public_key = tls_private_key.rsa.public_key_openssh
}

resource "local_file" "samiksha-gurukul" {
  content  = tls_private_key.rsa.private_key_pem
  filename = "tfkey.pem"
}
resource "aws_instance" "app_server" {
  ami           = "ami-00c39f71452c08778"
  instance_type = "t2.micro"
  key_name      = "samiksha-gurukul"
  tags = {
    Name = "Samiksha_EC2"
  }
}


resource "aws_security_group" "SG_allow" {

  name        = "SG_allow"
  description = "Security Group"
  vpc_id      = "vpc-019c09a1a0c5b4f6b"
  #HTTPS
  ingress {
    description      = "HTTPS"
    from_port        = 443
    to_port          = 443
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  ingress {
    description      = "HTTP"
    from_port        = 80
    to_port          = 80
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  ingress {
    description      = "SSH"
    from_port        = 22
    to_port          = 22
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  egress {
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }
  tags = {
    Name = "SG_allow"
  }
}
resource "aws_subnet" "gurukul_samiksha" {
  vpc_id     = "vpc-019c09a1a0c5b4f6b"
  cidr_block = "10.0.0.144/28"
  tags = {
    "Name" = "gurukul_samiksha"
  }
}
resource "aws_s3_bucket" "gurukul-samiksha" {
  bucket = "gurukul-samiksha"


  lifecycle {
    prevent_destroy = true
  }
}
resource "aws_s3_bucket_public_access_block" "public_access" {
  bucket                  = aws_s3_bucket.gurukul-samiksha.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
resource "aws_s3_bucket_versioning" "enabled" {
  bucket = aws_s3_bucket.gurukul-samiksha.id
  versioning_configuration {
    status = "Enabled"
  }
}
resource "aws_s3_bucket_server_side_encryption_configuration" "default" {
  bucket = aws_s3_bucket.gurukul-samiksha.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}



terraform {
  backend "s3" {

    bucket         = "gurukul-samiksha"
    key            = "global/s3/terraform.tfstate"
    region         = "us-east-1"

  }
}
output "s3_bucket_arn" {
  value       = aws_s3_bucket.gurukul-samiksha.arn
  description = "The ARN of the S3 bucket"
}

