package no.java.cupcake.plugins

import io.ktor.http.HttpMethod
import io.ktor.server.auth.OAuthServerSettings


fun slackProvider(clientId: String, clientSecret: String) = OAuthServerSettings.OAuth2ServerSettings(
    name = "slack",
    authorizeUrl = "https://slack.com/openid/connect/authorize",
    accessTokenUrl = "https://slack.com/api/openid.connect.token",
    clientId = clientId,
    clientSecret = clientSecret,
    accessTokenRequiresBasicAuth = false,
    requestMethod = HttpMethod.Post, // must POST to token endpoint
    defaultScopes = listOf("openid, profile, email")
)
