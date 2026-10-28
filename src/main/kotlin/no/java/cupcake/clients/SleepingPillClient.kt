package no.java.cupcake.clients

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json
import no.java.cupcake.config.SleepingPillConfig

// SleepingPill takes several seconds to stream a full session list; CIO's implicit 15s default is too tight
private const val REQUEST_TIMEOUT_MILLIS = 60_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

fun Application.sleepingPillClient(config: SleepingPillConfig): HttpClient =
    HttpClient(CIO) {
        install(Logging) {
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                },
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
            connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        }

        install(Auth) {
            basic {
                sendWithoutRequest { true }
                credentials {
                    BasicAuthCredentials(
                        username = config.username,
                        password = config.password,
                    )
                }
            }
        }

        defaultRequest {
            url(config.rootUrl)
        }
    }
