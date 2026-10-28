package no.java.cupcake.plugins

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.java.cupcake.CONFERENCE_2024_ID
import no.java.cupcake.buildClient
import no.java.cupcake.buildErrorMockEngine
import no.java.cupcake.buildSleepingPillService
import no.java.cupcake.buildTestClient
import no.java.cupcake.randomString
import no.java.cupcake.serializedTestApplication
import no.java.cupcake.sleepingpill.Conference
import no.java.cupcake.sleepingpill.Format
import no.java.cupcake.sleepingpill.Language
import no.java.cupcake.sleepingpill.Session
import no.java.cupcake.sleepingpill.SleepingPillService
import no.java.cupcake.sleepingpill.Speaker
import no.java.cupcake.sleepingpill.Status
import no.java.cupcake.sleepingpill.rejectSlugs
import no.java.cupcake.urlString

class RoutingTest :
    FunSpec({
        test("Fetch conferences") {
            testApplication {
                buildTestApplication(buildService("/conferences.json"))

                val client = buildTestClient()

                client
                    .get("/api/conferences") {
                        accept(ContentType.Application.Json)
                    }.apply {
                        status shouldBe HttpStatusCode.OK

                        val response = body<List<Conference>>()

                        response.size shouldBe 3

                        val conference2024 = response.first { it.slug == "javazone_2024" }

                        conference2024 shouldBe
                            Conference(
                                name = "JavaZone 2024",
                                slug = "javazone_2024",
                                id = "ad82e461-9444-40a4-a9d5-cc4885f9107a",
                            )

                        rejectSlugs.forEach { slug ->
                            response.filter { it.slug == slug } shouldBe emptyList()
                        }
                    }
            }
        }

        test("Fetch sessions") {
            testApplication {
                val id = CONFERENCE_2024_ID

                buildTestApplication(
                    buildService("/sessions.json") { request ->
                        if (request.url.encodedPath.endsWith("/session")) request.urlString() shouldContain id
                    },
                )

                val client = buildTestClient()

                client
                    .get("/api/conferences/$id/sessions") {
                        accept(ContentType.Application.Json)
                    }.apply {
                        status shouldBe HttpStatusCode.OK

                        val response = body<List<Session>>()

                        response.size shouldBe 1

                        with(response.first()) {
                            id shouldBe id
                            title shouldBe "Test talk 2024"
                            description shouldBe "My test description"
                            status shouldBe Status.SUBMITTED
                            format shouldBe Format.PRESENTATION
                            language shouldBe Language.NORWEGIAN
                            length shouldBe 60
                            speakers shouldBe
                                listOf(
                                    Speaker(
                                        name = "Test Testerson",
                                        email = "test@gmail.com",
                                        bio = "Hello I am me",
                                        postcode = "1555",
                                        location = "Norway",
                                        city = "Son",
                                        county = "Viken",
                                    ),
                                )
                        }
                    }
            }
        }

        test("Fetch sessions - unknown conference") {
            testApplication {
                buildTestApplication(buildService("/sessions.json"))

                buildTestClient()
                    .get("/api/conferences/${randomString()}/sessions") {
                        accept(ContentType.Application.Json)
                    }.apply {
                        status shouldBe HttpStatusCode.NotFound
                        bodyAsText() shouldContain "Conference not found"
                        headers[HttpHeaders.CacheControl] shouldBe null
                    }
            }
        }

        test("Successful conference data responses are privately cacheable for 5 minutes") {
            testApplication {
                buildTestApplication(buildService("/sessions.json"))

                val client = buildTestClient()

                listOf("/api/conferences", "/api/conferences/$CONFERENCE_2024_ID/sessions").forEach { path ->
                    client.get(path).apply {
                        status shouldBe HttpStatusCode.OK
                        headers[HttpHeaders.CacheControl] shouldBe "max-age=300, private"
                    }
                }
            }
        }

        test("Unhandled exceptions return a generic 500 without the exception text") {
            testApplication {
                serializedTestApplication {
                    configureRouting(sleepingPillService = buildService("/conferences.json"), securityOptional = true)
                    routing { get("/boom") { error("secret internal detail") } }
                }

                buildTestClient().get("/boom").apply {
                    status shouldBe HttpStatusCode.InternalServerError
                    bodyAsText() shouldContain "Internal server error"
                    bodyAsText() shouldNotContain "secret internal detail"
                }
            }
        }

        test("Fetch sessions - sleeping pill error") {
            testApplication {
                val id = CONFERENCE_2024_ID

                buildTestApplication(
                    buildService(
                        "/sessions.json",
                        client =
                            buildClient(
                                buildErrorMockEngine(HttpStatusCode.InternalServerError, "SleepingPill is asleep"),
                            ),
                    ),
                )

                val client = buildTestClient()

                client
                    .get("/api/conferences/$id/sessions") {
                        accept(ContentType.Application.Json)
                    }.apply {
                        status shouldBe HttpStatusCode.InternalServerError

                        val response = bodyAsText()

                        response shouldContain "SleepingPill is asleep"
                        response shouldContain "500"
                        response shouldContain "Internal Server Error"
                        response shouldContain "call to SleepingPill failed"
                    }
            }
        }
    })

private fun buildService(
    fixture: String,
    client: HttpClient? = null,
    block: (suspend (request: HttpRequestData) -> Unit)? = null,
) = buildSleepingPillService(fixture = fixture, client = client, block = block)

private fun ApplicationTestBuilder.buildTestApplication(service: SleepingPillService) {
    serializedTestApplication {
        configureRouting(sleepingPillService = service, securityOptional = true)
    }
}
