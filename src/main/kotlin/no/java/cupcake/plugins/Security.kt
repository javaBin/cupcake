package no.java.cupcake.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.oauth
import io.ktor.server.auth.principal
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.util.date.GMTDate
import no.java.cupcake.slack.SlackService
import no.java.cupcake.slack.SlackUser
import no.java.cupcake.str
import java.time.ZonedDateTime
import java.util.Date
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds


private const val slackAuth = "slack-oauth"
const val jwtAuth = "jwt-oauth"

private val cookieLifetime = 8.hours.inWholeMilliseconds


fun buildToken(env: ApplicationEnvironment, userInfo: SlackUser): String = JWT.create()
    .withAudience(env.str("jwt.audience"))
    .withIssuer(env.str("jwt.issuer"))
    .withClaim("slack_id", userInfo.userId)
    .withClaim("name", userInfo.name)
    .withClaim("email", userInfo.email)
    .withClaim("avatar", userInfo.avatar)
    .withExpiresAt(Date(System.currentTimeMillis() + cookieLifetime))
    .sign(Algorithm.HMAC256(env.str("jwt.secret")))

private fun ZonedDateTime.cookieExpiry(seconds: Long) =
    GMTDate(this.plusSeconds(seconds).toEpochSecond().seconds.inWholeMilliseconds)

fun Application.configureSecurity(
    provider: OAuthServerSettings.OAuth2ServerSettings,
    callback: String,
    slackService: SlackService,
    channelName: String
) {
    val jwtRealm = environment.str("jwt.realm")
    val jwtAudience = environment.str("jwt.audience")

    val redirect = environment.str("jwt.redirect")

    fun jwtVerifier(): JWTVerifier = JWT
        .require(Algorithm.HMAC256(environment.str("jwt.secret")))
        .withAudience(environment.str("jwt.audience"))
        .withIssuer(environment.str("jwt.issuer"))
        .build()

    install(Authentication) {
        oauth(slackAuth) {
            client = HttpClient(CIO)
            providerLookup = { provider }
            urlProvider = {
                callback
            }
        }

        jwt(jwtAuth) {
            realm = jwtRealm

            verifier(jwtVerifier())

            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) {
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
                                        true -> {
                                            val user = SlackUser(
                                                userId = userId,
                                                email = claims.str("email", "Unknown E-Mail"),
                                                name = claims.str("name", "Unknown Name"),
                                                avatar = claims.str("picture", "Unknown Avatar"),
                                                member = true
                                            )

                                            val jwt = buildToken(environment, user)

                                            call.response.cookies.append(
                                                name = "user_session",
                                                value = jwt,
                                                path = "/",
                                                expires = ZonedDateTime.now().cookieExpiry(cookieLifetime),
                                            )

                                            call.respondRedirect(redirect)
                                        }

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
