provider "aws" {
  region = "us-east-1"
}

variable "mongodb_uri" { type = string }
variable "mp_token" { type = string }

# 1. URI do Banco de Dados
resource "aws_ssm_parameter" "mongodb_uri" {
  name        = "/payment-service/mongodb-uri"
  description = "URI de conexao com o MongoDB Atlas"
  type        = "SecureString"
  value       = var.mongodb_uri
}

# 2. Token do Mercado Pago
resource "aws_ssm_parameter" "mp_token" {
  name        = "/payment-service/mp-token"
  type        = "SecureString"
  value       = var.mp_token
}
