package no.java.cupcake.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.java.cupcake.config.OidcConfig
import java.net.URI
import java.util.concurrent.TimeUnit

private const val JWK_BUCKET_SIZE = 10L
private const val JWK_REFILL_RATE = 10L
private const val JWK_CACHE_SIZE = 10L
private const val JWK_EXPIRES_IN = 24L

@Serializable
private data class OidcDiscovery(
    val issuer: String,
    @SerialName("jwks_uri") val jwksUri: String,
    @SerialName("userinfo_endpoint") val userInfoEndpoint: String,
)

@Serializable
private data class UserInfoResponse(
    val sub: String,
    val email: String? = null,
    val name: String? = null,
)

@Serializable
private data class UserInfo(
    val sub: String,
    val preferredUsername: String,
    val email: String,
    val groups: List<String>,
)

fun Application.configureAuth(oidcConfig: OidcConfig): String {
    val http =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    val oidc =
        runBlocking {
            http.get(oidcConfig.wellKnownUrl).body<OidcDiscovery>()
        }

    val jwkProvider =
        JwkProviderBuilder(URI(oidc.jwksUri).toURL())
            .cached(JWK_CACHE_SIZE, JWK_EXPIRES_IN, TimeUnit.HOURS)
            .rateLimited(JWK_BUCKET_SIZE, JWK_REFILL_RATE, TimeUnit.MINUTES)
            .build()

    install(Authentication) {
        jwt("javaBin") {
            realm = "cupcake"

            verifier(jwkProvider) {
                withIssuer(oidc.issuer)
            }

            validate { cred ->
                val groups =
                    cred.payload
                        .getClaim("cognito:groups")
                        ?.asList(String::class.java) ?: emptyList()
                if (groups.contains("helter")) JWTPrincipal(cred.payload) else null
            }

            challenge { _, _ ->
                call.respondText(
                    text = "Invalid or missing token",
                    status = HttpStatusCode.Unauthorized,
                )
            }
        }
    }

    return oidc.userInfoEndpoint
}

fun Application.configureUserInfoRoute(userInfoEndpoint: String) {
    val http =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    routing {
        authenticate("javaBin") {
            get("/api/me") {
                val p = call.principal<JWTPrincipal>()!!
                val token =
                    call.request.headers[HttpHeaders.Authorization]
                        ?.removePrefix("Bearer ") ?: ""

                val groups =
                    p.payload
                        .getClaim("cognito:groups")
                        ?.asList(String::class.java) ?: emptyList()

                val userInfo =
                    http
                        .get(userInfoEndpoint) {
                            header(HttpHeaders.Authorization, "Bearer $token")
                        }.body<UserInfoResponse>()

                call.respond(
                    UserInfo(
                        sub = p.payload.subject,
                        preferredUsername = userInfo.email ?: p.payload.subject,
                        email = userInfo.email ?: "",
                        groups = groups,
                    ),
                )
            }
        }
    }
}
