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
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.json.Json
import no.java.cupcake.bring.BringService
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.slack.SlackService
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

fun ApplicationTestBuilder.serializedTestApplication(block: Application.() -> Unit) {
    application {
        configureSerialization()
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
    MockEngine { request ->
        respond(
            content = ByteReadChannel(message),
            status = httpStatusCode,
            headers = headersOf(HttpHeaders.ContentType, "text/plain"),
        )
    }

fun buildSlackService(
    fixture: String,
    channel: String,
    membersUrl: String,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): SlackService =
    SlackService(
        botClient = buildClient(buildMockEngine(fixture, block)),
        channel = channel,
        membersUrl = membersUrl,
    )

fun buildSleepingPillService(
    fixture: String,
    client: HttpClient? = null,
    bringService: BringService? = null,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): SleepingPillService =
    SleepingPillService(
        client = client ?: buildClient(buildMockEngine(fixture, block)),
        bringService = bringService ?: buildBringService(fixture = "/postal_codes.json", postalCodeUrl = "/test"),
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
