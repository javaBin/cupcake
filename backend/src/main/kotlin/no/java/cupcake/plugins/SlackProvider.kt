package no.java.cupcake.plugins

import io.ktor.http.HttpMethod
import io.ktor.server.auth.OAuthServerSettings
import no.java.cupcake.config.SlackConfig

fun slackProvider(config: SlackConfig) =
    OAuthServerSettings.OAuth2ServerSettings(
        name = "slack",
        authorizeUrl = config.authUrl,
        accessTokenUrl = config.accessTokenUrl,
        clientId = config.clientId,
        clientSecret = config.clientSecret,
        accessTokenRequiresBasicAuth = false,
        requestMethod = HttpMethod.Post,
        defaultScopes = listOf("openid, profile, email"),
    )
