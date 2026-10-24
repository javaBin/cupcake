package no.java.cupcake.clients

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.java.cupcake.config.BringConfig

private const val BRING_API_KEY_HEADER = "X-Mybring-API-Key"

fun bringClient(bringConfig: BringConfig): HttpClient =
    HttpClient(CIO) {
        install(Logging) {
            sanitizeHeader { header -> header == BRING_API_KEY_HEADER }
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

        defaultRequest {
            header("X-Mybring-API-Uid", bringConfig.username)
            header(BRING_API_KEY_HEADER, bringConfig.apiKey)
        }
    }
