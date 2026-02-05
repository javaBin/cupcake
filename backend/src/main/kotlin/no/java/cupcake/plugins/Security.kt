package no.java.cupcake.plugins

import arrow.core.raise.either
import arrow.core.raise.ensure
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
import io.ktor.server.routing.Routing
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.date.GMTDate
import no.java.cupcake.api.CallPrincipalMissing
import no.java.cupcake.api.MissingChannelMembership
import no.java.cupcake.api.RefreshTokenInvalid
import no.java.cupcake.api.TokenMissing
import no.java.cupcake.api.TokenMissingUser
import no.java.cupcake.api.redirect
import no.java.cupcake.api.respond
import no.java.cupcake.config.JwtConfig
import no.java.cupcake.slack.SlackService
import no.java.cupcake.slack.SlackUser
import java.time.ZonedDateTime
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val SLACK_AUTH = "slack-oauth"
const val JWT_AUTH = "jwt-oauth"

private val idTokenLifetime = 8.hours

private fun Map<String, Claim>.str(
    key: String,
    missing: String,
) = this[key]?.asString() ?: missing

private fun Map<String, Claim>.toSlackUser(
    userId: String,
    member: Boolean,
) = SlackUser(
    userId = userId,
    email = this.str("email", "Unknown E-Mail"),
    name = this.str("name", "Unknown Name"),
    avatar = this.str("picture", "Unknown Avatar"),
    member = member,
)

fun buildIdToken(
    jwtConfig: JwtConfig,
    userInfo: SlackUser,
): String =
    JWT
        .create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("type", "ID")
        .withClaim("slack_id", userInfo.userId)
        .withClaim("name", userInfo.name)
        .withClaim("email", userInfo.email)
        .withClaim("avatar", userInfo.avatar)
        .withExpiresAt(Date(System.currentTimeMillis() + idTokenLifetime.inWholeMilliseconds))
        .sign(Algorithm.HMAC256(jwtConfig.secret))

fun buildAccessToken(
    jwtConfig: JwtConfig,
    slackId: String,
): String =
    JWT
        .create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("type", "ACCESS")
        .withClaim("slack_id", slackId)
        .withExpiresAt(
            Date(
                System.currentTimeMillis() +
                    jwtConfig.accessTokenLifetimeMinutes.minutes.inWholeMilliseconds,
            ),
        ).sign(Algorithm.HMAC256(jwtConfig.secret))

fun buildRefreshToken(
    jwtConfig: JwtConfig,
    slackId: String,
): String =
    JWT
        .create()
        .withAudience(jwtConfig.audience)
        .withIssuer(jwtConfig.issuer)
        .withClaim("type", "REFRESH")
        .withClaim("slack_id", slackId)
        .withExpiresAt(
            Date(
                System.currentTimeMillis() +
                    jwtConfig.refreshTokenLifetimeMinutes.minutes.inWholeMilliseconds,
            ),
        ).sign(Algorithm.HMAC256(jwtConfig.secret))

private fun ZonedDateTime.cookieExpiry(duration: Duration) =
    GMTDate(
        this
            .plusSeconds(duration.inWholeSeconds)
            .toEpochSecond()
            .seconds.inWholeMilliseconds,
    )

fun Application.configureSecurity(
    provider: OAuthServerSettings.OAuth2ServerSettings,
    callback: String,
    slackService: SlackService,
    channelName: String,
    jwtConfig: JwtConfig,
) {
    fun jwtVerifier(): JWTVerifier =
        JWT
            .require(Algorithm.HMAC256(jwtConfig.secret))
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
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
                val type = credential.payload.getClaim("type")?.asString()
                if (type == "ACCESS" && credential.payload.audience.contains(jwtConfig.audience)) {
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
            slackService = slackService,
            redirect = jwtConfig.redirect,
            channelName = channelName,
            jwtConfig = jwtConfig,
            jwtVerifier = jwtVerifier(),
        )
    }
}

private fun RoutingContext.setSessionCookies(
    jwtConfig: JwtConfig,
    slackId: String,
) {
    val now = ZonedDateTime.now()
    val accessToken = buildAccessToken(jwtConfig = jwtConfig, slackId = slackId)
    val refreshToken = buildRefreshToken(jwtConfig = jwtConfig, slackId = slackId)

    call.response.cookies.append(
        name = "access_token",
        value = accessToken,
        path = "/",
        expires = now.cookieExpiry(jwtConfig.accessTokenLifetimeMinutes.minutes),
    )
    call.response.cookies.append(
        name = "refresh_token",
        value = refreshToken,
        path = "/",
        httpOnly = true,
        expires = now.cookieExpiry(jwtConfig.refreshTokenLifetimeMinutes.minutes),
    )
}

private fun Routing.configureAuthRouting(
    slackService: SlackService,
    redirect: String,
    channelName: String,
    jwtConfig: JwtConfig,
    jwtVerifier: JWTVerifier,
) {
    authenticate(SLACK_AUTH) {
        get("/login") {
            // Redirects for authentication
        }
        get("/slackCallback") {
            either {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()

                ensure(principal != null) {
                    CallPrincipalMissing
                }

                val slackIdToken = principal.extraParameters["id_token"]

                ensure(slackIdToken != null) {
                    TokenMissing
                }

                val claims: MutableMap<String, Claim> = JWT.decode(slackIdToken).claims

                val userId = claims["https://slack.com/user_id"]?.asString()

                ensure(userId != null) {
                    TokenMissingUser
                }

                val isMember = slackService.isMember(id = userId, raise = this)

                ensure(isMember) {
                    MissingChannelMembership(channelName)
                }

                val user = claims.toSlackUser(userId, true)

                call.response.cookies.append(
                    name = "id_token",
                    value = buildIdToken(jwtConfig = jwtConfig, userInfo = user),
                    path = "/",
                    expires = ZonedDateTime.now().cookieExpiry(idTokenLifetime),
                )

                setSessionCookies(jwtConfig, userId)

                // Expire old user_session cookie
                call.response.cookies.append(
                    name = "user_session",
                    value = "",
                    path = "/",
                    maxAge = 0,
                )

                redirect
            }.redirect()
        }
    }

    refreshRouting(jwtVerifier, jwtConfig)
}

private fun Routing.refreshRouting(
    jwtVerifier: JWTVerifier,
    jwtConfig: JwtConfig,
) {
    post("/refresh") {
        val refreshCookie = call.request.cookies["refresh_token"]

        if (refreshCookie == null) {
            respond(RefreshTokenInvalid)
            return@post
        }

        try {
            val decoded = jwtVerifier.verify(refreshCookie)
            val type = decoded.getClaim("type")?.asString()
            val slackId = decoded.getClaim("slack_id")?.asString()

            if (type != "REFRESH" || slackId == null) {
                respond(RefreshTokenInvalid)
                return@post
            }

            setSessionCookies(jwtConfig, slackId)

            call.respond(HttpStatusCode.OK, mapOf("status" to "refreshed"))
        } catch (_: Exception) {
            respond(RefreshTokenInvalid)
        }
    }
}
