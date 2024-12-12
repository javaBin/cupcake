package no.java.cupcake.plugins

import io.ktor.http.HttpMethod
import io.ktor.server.auth.OAuthServerSettings

fun slackProvider(
    clientId: String,
    clientSecret: String,
    authUrl: String,
    accessTokenUrl: String,
) = OAuthServerSettings.OAuth2ServerSettings(
    name = "slack",
    authorizeUrl = authUrl,
    accessTokenUrl = accessTokenUrl,
    clientId = clientId,
    clientSecret = clientSecret,
    accessTokenRequiresBasicAuth = false,
    requestMethod = HttpMethod.Post,
    defaultScopes = listOf("openid, profile, email"),
)
