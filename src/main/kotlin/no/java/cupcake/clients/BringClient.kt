package no.java.cupcake.clients

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json
import no.java.cupcake.config.BringConfig

fun Application.bringClient(bringConfig: BringConfig): HttpClient {
    return HttpClient(CIO) {
        install(Logging)

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
            header("X-Mybring-API-Key", bringConfig.apiKey)
        }
    }
}
