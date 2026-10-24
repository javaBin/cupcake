# GENERATED FROM app.yaml — do not edit, changes will be overwritten
module "platform" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/platform-data?ref=main"

  project = "javabin"
  domain  = "javazone.no"
  team    = "cupcake"
}

module "ecr" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/ecr-repo?ref=main"

  name = "cupcake-backend"
  team = "cupcake"
}

module "routing" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-routing?ref=main"

  name                   = "cupcake-backend"
  team                   = "cupcake"
  vpc_id                 = module.platform.vpc_id
  port                   = 8080
  health_check_path      = "/health"
  health_check_matcher   = "200"
  https_listener_arn     = module.platform.https_listener_arn
  listener_rule_priority = 101
  host_header            = "cupcake-backend.javazone.no"
  route53_zone_id        = module.platform.route53_zone_ids["javazone.no"]
  alb_dns_name           = module.platform.alb_dns_name
  alb_zone_id            = module.platform.alb_zone_id
  deregistration_delay   = 30
}

module "secret_sp_user" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "sp_user"
  team        = "cupcake"
  project     = "javabin"
  service     = "cupcake-backend"
  description = ""
}

module "secret_sp_password" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "sp_password"
  team        = "cupcake"
  project     = "javabin"
  service     = "cupcake-backend"
  description = ""
}

module "secret_bring_api_user" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "bring_api_user"
  team        = "cupcake"
  project     = "javabin"
  service     = "cupcake-backend"
  description = ""
}

module "secret_bring_api_key" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-secret?ref=main"

  name        = "bring_api_key"
  team        = "cupcake"
  project     = "javabin"
  service     = "cupcake-backend"
  description = ""
}

module "auth" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/cognito-app-client?ref=main"

  app_name    = "cupcake-backend"
  team        = "cupcake"
  pool        = "internal"
  client_type = "none"
  project     = "javabin"
}

module "task_role" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-role?ref=main"

  name                     = "cupcake-backend"
  team                     = "cupcake"
  region                   = "eu-central-1"
  aws_account_id           = "553637109631"
  permissions_boundary_arn = module.platform.developer_boundary_arn
  additional_policy_jsons = {
    secret_sp_user        = module.secret_sp_user.access_policy_json
    secret_sp_password    = module.secret_sp_password.access_policy_json
    secret_bring_api_user = module.secret_bring_api_user.access_policy_json
    secret_bring_api_key  = module.secret_bring_api_key.access_policy_json
  }
}

module "service" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/ecs-service?ref=main"

  name                     = "cupcake-backend"
  team                     = "cupcake"
  cluster_id               = module.platform.ecs_cluster_id
  image                    = module.ecr.repository_url
  cpu                      = 512
  memory                   = 1024
  port                     = 8080
  desired_count            = 1
  execution_role_arn       = module.platform.execution_role_arn
  task_role_arn            = module.task_role.role_arn
  subnet_ids               = module.platform.private_subnet_ids
  security_group_ids       = [module.platform.ecs_tasks_security_group_id]
  region                   = "eu-central-1"
  container_user           = "1000"
  readonly_root_filesystem = true
  target_group_arn         = module.routing.target_group_arn
  environment = {
    SP_BASE_URL            = "https://sleepingpill.javazone.no"
    COGNITO_USER_POOL_ID   = module.auth.user_pool_id
    COGNITO_DOMAIN         = module.auth.user_pool_domain
    COGNITO_ISSUER_URL     = module.auth.issuer_url
    COGNITO_WELL_KNOWN_URL = module.auth.well_known_url
    OIDC_WELL_KNOWN_URL    = module.auth.well_known_url
  }
  secrets = {
    SP_USER        = module.secret_sp_user.parameter_arn
    SP_PASSWORD    = module.secret_sp_password.parameter_arn
    BRING_API_USER = module.secret_bring_api_user.parameter_arn
    BRING_API_KEY  = module.secret_bring_api_key.parameter_arn
  }
}

module "alarms" {
  source = "git::https://github.com/javaBin/platform//terraform/modules/service-alarm?ref=main"

  name                    = "cupcake-backend"
  team                    = "cupcake"
  service                 = "cupcake-backend"
  cluster_name            = module.platform.ecs_cluster_name
  sns_topic_arns          = [data.aws_sns_topic.alerts.arn]
  cpu_threshold           = 80
  memory_threshold        = 80
  error_5xx_threshold     = 10
  target_group_arn_suffix = module.routing.target_group_arn_suffix
  alb_arn_suffix          = module.platform.alb_arn_suffix
}

data "aws_sns_topic" "alerts" {
  name = "javabin-alerts"
}
