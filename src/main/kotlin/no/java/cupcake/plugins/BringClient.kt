package no.java.cupcake.plugins

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json
import no.java.cupcake.str

fun Application.bringClient(): HttpClient {
    val username = environment.str("bring.username")
    val apiKey = environment.str("bring.api_key")

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
            header("X-Mybring-API-Uid", username)
            header("X-Mybring-API-Key", apiKey)
        }
    }
}
