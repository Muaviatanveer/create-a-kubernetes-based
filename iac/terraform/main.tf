```hcl
provider "aws" {
  region = var.aws_region
}

provider "azure" {
  subscription_id = var.azure_subscription_id
  client_id       = var.azure_client_id
  client_secret   = var.azure_client_secret
  tenant_id       = var.azure_tenant_id
}

provider "google" {
  credentials = file(var.google_credentials_file)
  project     = var.google_project_id
  region      = var.google_region
}

resource "aws_eks_cluster" "example_aws" {
  name     = var.cluster_name
  role_arn = aws_iam_role.example.arn

  vpc_config {
    subnet_ids = var.subnet_ids
  }

  depends_on = [aws_iam_role_policy_attachment.example]
}

resource "azure_container_service_kubernetes" "example_azure" {
  name                = var.cluster_name
  resource_group_name = var.resource_group_name
  location            = var.azure_location

  agent_pool_profile {
    name       = "agentpool"
    count      = var.agent_count
    vm_size    = var.vm_size
    os_disk_size_gb = var.os_disk_size_gb
  }

  service_principal {
    client_id     = var.client_id
    client_secret = var.client_secret
  }
}

resource "google_container_cluster" "example_google" {
  name     = var.cluster_name
  location = var.google_location

  node_pool {
    name       = "default-pool"
    machine_type = var.machine_type
    node_count = var.node_count
  }

  oauth_config {
    client_id = var.client_id
  }
}

resource "aws_iam_role" "example" {
  name = "${var.cluster_name}-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole",
        Effect = "Allow",
        Principal = {
          Service = "eks.amazonaws.com"
        }
      },
    ]
  })
}

resource "aws_iam_role_policy_attachment" "example" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.example.name
}
```

This Terraform configuration defines a Kubernetes cluster in AWS, Azure, and Google Cloud. It includes the necessary providers, resources, and dependencies to create a multi-cloud Kubernetes cluster. The configuration uses variables for flexibility and security best practices, such as storing sensitive information like credentials in environment variables or secure vaults.