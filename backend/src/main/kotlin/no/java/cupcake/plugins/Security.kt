package no.java.cupcake.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
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

fun Application.configureAuth(oidcConfig: OidcConfig) {
    @Serializable
    data class OidcConfig(
        val issuer: String,
        @SerialName("jwks_uri") val jwksUri: String,
    )

    val http =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

    val oidc =
        runBlocking {
            http.get(oidcConfig.wellKnownUrl).body<OidcConfig>()
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
                val azp = cred.payload.getClaim("azp")?.asString()
                if (azp != oidcConfig.expectedAzp) return@validate null

                @Suppress("UNCHECKED_CAST")
                val clientRoles =
                    (cred.payload.getClaim("resource_access")?.asMap()?.get(oidcConfig.expectedAzp) as? Map<String, Any>)
                        ?.get("roles") as? List<*>

                if (clientRoles?.contains("pkom") == true) JWTPrincipal(cred.payload) else null
            }

            challenge { _, _ ->
                call.respondText(
                    text = "Invalid or missing token",
                    status = HttpStatusCode.Unauthorized,
                )
            }
        }
    }
}

@Serializable
private data class UserInfo(
    val sub: String,
    val preferredUsername: String,
    val email: String?,
    val hasPkomRole: Boolean,
)

fun Application.configureUserInfoRoute(oidcConfig: OidcConfig) {
    routing {
        authenticate("javaBin") {
            get("/api/me") {
                val p = call.principal<JWTPrincipal>()!!

                @Suppress("UNCHECKED_CAST")
                val clientRoles =
                    (p.payload.getClaim("resource_access")?.asMap()?.get(oidcConfig.expectedAzp) as? Map<String, Any>)
                        ?.get("roles") as? List<*>

                call.respond(
                    UserInfo(
                        sub = p.payload.subject,
                        preferredUsername = p.payload.getClaim("preferred_username").asString(),
                        email = p.payload.getClaim("email")?.asString(),
                        hasPkomRole = clientRoles?.contains("pkom") == true,
                    ),
                )
            }
        }
    }
}
