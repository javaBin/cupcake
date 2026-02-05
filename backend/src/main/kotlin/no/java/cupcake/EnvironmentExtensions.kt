package no.java.cupcake

import io.ktor.server.application.ApplicationEnvironment
import no.java.cupcake.config.BringConfig
import no.java.cupcake.config.JwtConfig
import no.java.cupcake.config.SlackConfig
import no.java.cupcake.config.SleepingPillConfig

fun ApplicationEnvironment.bringConfig() =
    BringConfig(
        username = str("bring.username"),
        apiKey = str("bring.api_key"),
    )

fun ApplicationEnvironment.sleepingPillConfig() =
    SleepingPillConfig(
        username = str("sleepingpill.username"),
        password = str("sleepingpill.password"),
        rootUrl = str("sleepingpill.base"),
        cacheTtlSeconds = long("sleepingpill.cache_ttl_seconds"),
    )

fun ApplicationEnvironment.jwtConfig() =
    JwtConfig(
        realm = str("jwt.realm"),
        audience = str("jwt.audience"),
        secret = str("jwt.secret"),
        issuer = str("jwt.issuer"),
        redirect = str("jwt.redirect"),
        accessTokenLifetimeMinutes = long("jwt.access_token_lifetime_minutes"),
        refreshTokenLifetimeMinutes = long("jwt.refresh_token_lifetime_minutes"),
    )

fun ApplicationEnvironment.slackConfig() =
    SlackConfig(
        clientId = str("slack.client"),
        clientSecret = str("slack.secret"),
        authUrl = str("slack.authorize_url"),
        accessTokenUrl = str("slack.accesstoken_url"),
    )
