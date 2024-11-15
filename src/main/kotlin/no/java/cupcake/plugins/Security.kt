package no.java.cupcake.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.Claim
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import no.java.cupcake.slack.SlackService


private const val slackAuth = "slack-oauth"

@Serializable
data class SlackUser(
    val userId: String,
    val email: String,
    val name: String,
    val avatar: String,
    val member: Boolean
)

fun Application.configureSecurity(
    provider: OAuthServerSettings.OAuth2ServerSettings,
    callback: String,
    slackService: SlackService,
    channelName: String
) {

    install(Authentication) {
        oauth(slackAuth) {
            client = HttpClient(CIO)
            providerLookup = { provider }
            urlProvider = {
                callback
            }
        }
    }

    fun Map<String, Claim>.str(key: String, missing: String) = this[key]?.asString() ?: missing

    routing {
        authenticate(slackAuth) {
            get("/login") {
                // Redirects for authentication
            }
            get("/slackCallback") {
                when (val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()) {
                    null -> call.respond(HttpStatusCode.Unauthorized, "Could not parse slack response")
                    else -> {
                        val idToken = principal.extraParameters["id_token"]

                        idToken?.let {
                            val claims: MutableMap<String, Claim> = JWT.decode(idToken).claims

                            when (val userId = claims["https://slack.com/user_id"]?.asString()) {
                                null -> call.respond(HttpStatusCode.Unauthorized, "No user found in slack response")
                                else -> {

                                    when (slackService.isMember(userId)) {
                                        true -> call.respond(
                                            SlackUser(
                                                userId = userId,
                                                email = claims.str("email", "Unknown E-Mail"),
                                                name = claims.str("name", "Unknown Name"),
                                                avatar = claims.str("picture", "Unknown Avatar"),
                                                member = true
                                            )
                                        )

                                        else -> call.respond(
                                            HttpStatusCode.Unauthorized,
                                            "User not in correct slack channel - please ask in #kodesmia for access to $channelName"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
