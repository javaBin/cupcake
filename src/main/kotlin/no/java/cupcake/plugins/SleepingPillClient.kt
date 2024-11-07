package no.java.cupcake.plugins

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import kotlinx.serialization.json.Json

fun Application.sleepingPillClient(): HttpClient {
    val username = environment.config.property("sleepingpill.username").getString()
    val password = environment.config.property("sleepingpill.password").getString()

    val rootUrl = environment.config.property("sleepingpill.base").getString()

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

        install(Auth) {
            basic {
                sendWithoutRequest { true }
                credentials {
                    BasicAuthCredentials(
                        username = username,
                        password = password,
                    )
                }
            }
        }

        defaultRequest {
            url(rootUrl)
        }
    }
}
