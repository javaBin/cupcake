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
    )

fun ApplicationEnvironment.jwtConfig() =
    JwtConfig(
        realm = str("jwt.realm"),
        audience = str("jwt.audience"),
        secret = str("jwt.secret"),
        issuer = str("jwt.issuer"),
        redirect = str("jwt.redirect"),
    )

fun ApplicationEnvironment.slackConfig() =
    SlackConfig(
        clientId = str("slack.client"),
        clientSecret = str("slack.secret"),
        authUrl = str("slack.authorize_url"),
        accessTokenUrl = str("slack.accesstoken_url"),
    )
