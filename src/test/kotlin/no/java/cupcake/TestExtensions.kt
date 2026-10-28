package no.java.cupcake

import com.github.benmanes.caffeine.cache.Ticker
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
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
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

fun randomString() = UUID.randomUUID().toString()

// JavaZone 2024 in conferences.json - inside the conference window for the default test clock
const val CONFERENCE_2024_ID = "ad82e461-9444-40a4-a9d5-cc4885f9107a"

class FakeTicker : Ticker {
    private val nanos = AtomicLong()

    override fun read() = nanos.get()

    fun advance(duration: Duration) {
        nanos.addAndGet(duration.inWholeNanoseconds)
    }
}

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

private val oslo = ZoneId.of("Europe/Oslo")

fun fixedClock(date: String): Clock = Clock.fixed(LocalDate.parse(date).atStartOfDay(oslo).toInstant(), oslo)

private fun MockRequestHandleScope.respondFixture(fixture: String) =
    respond(
        content = ByteReadChannel(loadFixture(fixture)),
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

fun buildMockEngine(
    fixture: String,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): MockEngine =
    MockEngine { request ->
        block?.invoke(request)

        respondFixture(fixture)
    }

// Serves /public/config from configFixture (404 when null), the conference list from conferences.json
// and everything else from fixture
fun buildSleepingPillMockEngine(
    fixture: String,
    configFixture: String?,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): MockEngine =
    MockEngine { request ->
        block?.invoke(request)

        when {
            request.url.encodedPath == "/data/conference" -> respondFixture("/conferences.json")
            request.url.encodedPath != "/public/config" -> respondFixture(fixture)
            configFixture != null -> respondFixture(configFixture)
            else -> respond(content = "Not found", status = HttpStatusCode.NotFound)
        }
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
    configFixture: String? = "/public_config_2026.json",
    clock: Clock = fixedClock("2026-10-24"),
    ticker: Ticker = Ticker.systemTicker(),
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
): SleepingPillService =
    SleepingPillService(
        client = client ?: buildClient(buildSleepingPillMockEngine(fixture, configFixture, block)),
        bringService = buildBringService(fixture = "/postal_codes.json", postalCodeUrl = "/test"),
        cacheTimeoutSeconds = 10,
        maxPastYears = 3,
        clock = clock,
        ticker = ticker,
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
