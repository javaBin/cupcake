package no.java.cupcake

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import no.java.cupcake.plugins.configureSerialization
import java.util.UUID


fun randomString() = UUID.randomUUID().toString()

fun buildClient(engine: MockEngine): HttpClient {
    return HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }
    }
}

fun ApplicationTestBuilder.buildTestClient() = createClient {
    this.install(ContentNegotiation) {
        json()
    }
}

fun ApplicationTestBuilder.serializedTestApplication(block: Application.() -> Unit) {
    application {
        configureSerialization()
        block()
    }
}

fun HttpRequestData.urlString() = url.toString()

fun loadFixture(path: String): String =
    object {}.javaClass.getResource(path)!!.readText()
