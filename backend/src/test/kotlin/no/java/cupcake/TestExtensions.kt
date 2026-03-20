package no.java.cupcake

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationContext
import io.ktor.server.auth.AuthenticationProvider
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import no.java.cupcake.bring.BringService
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.sleepingpill.SleepingPillService
import java.util.UUID

fun randomString() = UUID.randomUUID().toString()

fun buildClient(engine: MockEngine): HttpClient =
    HttpClient(engine) {
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
    }

fun ApplicationTestBuilder.buildTestClient() =
    createClient {
        this.install(ContentNegotiation) {
            json()
        }
    }

private class NoOpAuthProvider(
    name: String,
) : AuthenticationProvider(object : Config(name) {}) {
    override suspend fun onAuthenticate(context: AuthenticationContext) = Unit
}

fun ApplicationTestBuilder.serializedTestApplication(block: Application.() -> Unit) {
    application {
        configureSerialization()
        install(Authentication) {
            register(NoOpAuthProvider("javaBin"))
        }
        block()
    }
}

fun HttpRequestData.urlString() = url.toString()

fun loadFixture(path: String): String = object {}.javaClass.getResource(path)!!.readText()

fun buildMockEngine(
    fixture: String,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): MockEngine =
    MockEngine { request ->
        block?.invoke(request)

        respond(
            content = ByteReadChannel(loadFixture(fixture)),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }

fun buildErrorMockEngine(
    httpStatusCode: HttpStatusCode,
    message: String,
): MockEngine =
    MockEngine { _ ->
        respond(
            content = ByteReadChannel(message),
            status = httpStatusCode,
            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
        )
    }

fun buildSleepingPillService(
    fixture: String,
    client: HttpClient? = null,
    bringService: BringService? = null,
    includeCurrentYear: Boolean = false,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): SleepingPillService =
    SleepingPillService(
        client = client ?: buildClient(buildMockEngine(fixture, block)),
        bringService = bringService ?: buildBringService(fixture = "/postal_codes.json", postalCodeUrl = "/test"),
        cacheTimeoutSeconds = 10,
        maxPastYears = 3,
        includeCurrentYear = includeCurrentYear,
        initAtStart = false,
    )

fun buildBringService(
    fixture: String,
    postalCodeUrl: String,
    client: HttpClient? = null,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): BringService =
    BringService(
        client = client ?: buildClient(buildMockEngine(fixture, block)),
        postalCodeUrl = postalCodeUrl,
        scheduler = false,
    )
