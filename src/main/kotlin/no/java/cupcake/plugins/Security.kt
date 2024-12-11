package no.java.cupcake.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.util.date.GMTDate
import no.java.cupcake.config.JwtConfig
import no.java.cupcake.slack.SlackService
import no.java.cupcake.slack.SlackUser
import java.time.ZonedDateTime
import java.util.Date
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


private const val SLACK_AUTH = "slack-oauth"
const val JWT_AUTH = "jwt-oauth"

private val cookieLifetime = 8.hours.inWholeMilliseconds

private fun Map<String, Claim>.str(key: String, missing: String) = this[key]?.asString() ?: missing

private fun Map<String, Claim>.toSlackUser(userId: String, member: Boolean) = SlackUser(
    userId = userId,
    email = this.str("email", "Unknown E-Mail"),
    name = this.str("name", "Unknown Name"),
    avatar = this.str("picture", "Unknown Avatar"),
    member = member
)

fun buildToken(
    jwtAudience: String,
    jwtSecret: String,
    jwtIssuer: String,
    userInfo: SlackUser
): String =
    JWT.create().withAudience(jwtAudience).withIssuer(jwtIssuer)
        .withClaim("slack_id", userInfo.userId).withClaim("name", userInfo.name).withClaim("email", userInfo.email)
        .withClaim("avatar", userInfo.avatar).withExpiresAt(Date(System.currentTimeMillis() + cookieLifetime))
        .sign(Algorithm.HMAC256(jwtSecret))

private fun ZonedDateTime.cookieExpiry(seconds: Long) =
    GMTDate(this.plusSeconds(seconds).toEpochSecond().seconds.inWholeMilliseconds)

fun Application.configureSecurity(
    provider: OAuthServerSettings.OAuth2ServerSettings,
    callback: String,
    slackService: SlackService,
    channelName: String,
    jwtConfig: JwtConfig,
) {
    fun jwtVerifier(): JWTVerifier =
        JWT.require(Algorithm.HMAC256(jwtConfig.secret)).withAudience(jwtConfig.audience).withIssuer(jwtConfig.issuer)
            .build()

    install(Authentication) {
        oauth(SLACK_AUTH) {
            client = HttpClient(CIO)
            providerLookup = { provider }
            urlProvider = {
                callback
            }
        }

        jwt(JWT_AUTH) {
            realm = jwtConfig.realm

            verifier(jwtVerifier())

            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience)) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    routing {
        configureAuthRouting(
            slackService = slackService, redirect = jwtConfig.redirect, channelName = channelName,
            jwtConfig = jwtConfig,
        )
    }
}

private fun Routing.configureAuthRouting(
    slackService: SlackService,
    redirect: String,
    channelName: String,
    jwtConfig: JwtConfig,
) {
    authenticate(SLACK_AUTH) {
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
                                    true -> {
                                        val user = claims.toSlackUser(userId, true)

                                        val jwt = buildToken(
                                            jwtAudience = jwtConfig.audience,
                                            jwtSecret = jwtConfig.secret,
                                            jwtIssuer = jwtConfig.issuer,
                                            userInfo = user,
                                        )

                                        call.response.cookies.append(
                                            name = "user_session",
                                            value = jwt,
                                            path = "/",
                                            expires = ZonedDateTime.now().cookieExpiry(cookieLifetime),
                                        )

                                        call.respondRedirect(redirect)
                                    }

                                    else -> missingChannelMembership(channelName)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun RoutingContext.missingChannelMembership(channelName: String) {
    call.respond(
        HttpStatusCode.Unauthorized,
        "User not in correct slack channel - please ask in #kodesmia for access to $channelName"
    )
}
