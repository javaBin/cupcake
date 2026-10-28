package no.java.cupcake.sleepingpill

import arrow.core.raise.either
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import no.java.cupcake.CONFERENCE_2024_ID
import no.java.cupcake.FakeTicker
import no.java.cupcake.api.ConferenceIdRequired
import no.java.cupcake.api.ConferenceNotFound
import no.java.cupcake.api.SleepingPillUnreachable
import no.java.cupcake.buildClient
import no.java.cupcake.buildSleepingPillMockEngine
import no.java.cupcake.buildSleepingPillService
import no.java.cupcake.fixedClock
import no.java.cupcake.randomString
import java.io.IOException
import java.time.Clock
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.seconds

class SleepingPillServiceTest :
    FunSpec({
        test("Can fetch conference list") {
            val service = buildService("/conferences.json")

            val conferences =
                either {
                    service.conferences()
                }

            conferences.isRight() shouldBe true

            val conference2024 = conferences.getOrNull()!!.first { it.slug == "javazone_2024" }

            conference2024 shouldBe
                Conference(
                    name = "JavaZone 2024",
                    slug = "javazone_2024",
                    id = "ad82e461-9444-40a4-a9d5-cc4885f9107a",
                )
        }

        test("Includes the conference year once the conference is complete") {
            buildService("/conferences.json", clock = fixedClock("2026-10-24")).slugs() shouldBe
                listOf("javazone_2024", "javazone_2025", "javazone_2026")
        }

        test("Excludes the conference year before the conference is complete") {
            buildService("/conferences.json", clock = fixedClock("2026-08-15")).slugs() shouldBe
                listOf("javazone_2023", "javazone_2024", "javazone_2025")
        }

        test("Excludes the conference year on the last conference day") {
            buildService("/conferences.json", clock = fixedClock("2026-09-03")).slugs() shouldBe
                listOf("javazone_2023", "javazone_2024", "javazone_2025")
        }

        test("Falls back to last year when public config is unavailable") {
            buildService("/conferences.json", configFixture = null, clock = fixedClock("2026-10-24")).slugs() shouldBe
                listOf("javazone_2023", "javazone_2024", "javazone_2025")
        }

        test("Rejected conferences are excluded from the window") {
            val slugs =
                buildService(
                    "/conferences.json",
                    configFixture = null,
                    clock = fixedClock("2022-06-01"),
                ).slugs()

            slugs shouldBe listOf("javazone_2019", "javazone_2021")

            rejectSlugs.forEach { slug -> slugs shouldNotContain slug }
        }

        test("Can fetch session list") {
            val service = buildService("/sessions.json")

            val sessions =
                either {
                    service.sessions(ConferenceId(CONFERENCE_2024_ID))
                }

            sessions.isRight() shouldBe true

            val sessionList = sessions.getOrNull()!!

            sessionList.size shouldBe 1

            with(sessionList.first()) {
                id shouldBe "57f8dbb5-af4b-453f-b0c2-14067aae21b8"
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

        test("Gives correct error if conference id is missing") {
            val service = buildService("/sessions.json")

            val sessions =
                either {
                    service.sessions(ConferenceId(null))
                }

            sessions.isRight() shouldBe false
            sessions.swap().getOrNull()!! shouldBe ConferenceIdRequired
        }

        test("Unknown conference id is rejected without reading sessions from SleepingPill") {
            val calls = ConcurrentLinkedQueue<String>()
            val service = buildSleepingPillService("/sessions.json") { calls.add(it.url.encodedPath) }

            val sessions = either { service.sessions(ConferenceId(randomString())) }

            sessions.swap().getOrNull().shouldBeInstanceOf<ConferenceNotFound>()
            calls.sessionReads() shouldBe 0
        }

        test("Repeated calls within the TTL read SleepingPill once") {
            val calls = ConcurrentLinkedQueue<String>()
            val service = buildSleepingPillService("/sessions.json") { calls.add(it.url.encodedPath) }

            repeat(10) {
                either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) }.isRight() shouldBe true
            }

            calls.conferenceReads() shouldBe 1
            calls.sessionReads() shouldBe 1
        }

        test("Past the TTL the cached value is served while one background refresh runs") {
            val calls = ConcurrentLinkedQueue<String>()
            val ticker = FakeTicker()
            val service =
                buildSleepingPillService("/sessions.json", ticker = ticker) { calls.add(it.url.encodedPath) }

            val first = either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) }.getOrNull()!!

            ticker.advance(11.seconds)

            either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) }.getOrNull() shouldBe first

            eventually(5.seconds) { calls.sessionReads() shouldBe 2 }

            repeat(5) { either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) } }

            calls.sessionReads() shouldBe 2
        }

        test("A failed refresh keeps serving the cached value") {
            val failing = AtomicBoolean(false)
            val calls = ConcurrentLinkedQueue<String>()
            val ticker = FakeTicker()
            val healthy = buildSleepingPillMockEngine("/sessions.json", "/public_config_2026.json")
            val engine =
                MockEngine { request ->
                    calls.add(request.url.encodedPath)
                    if (failing.get()) {
                        respond(content = "down", status = HttpStatusCode.ServiceUnavailable)
                    } else {
                        healthy.config.requestHandlers.first()(this, request)
                    }
                }
            val service =
                buildSleepingPillService("/sessions.json", client = buildClient(engine), ticker = ticker)

            val first = either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) }.getOrNull()!!

            failing.set(true)
            ticker.advance(11.seconds)

            either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) }.getOrNull() shouldBe first
            eventually(5.seconds) { calls.sessionReads() shouldBe 2 }
            either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) }.getOrNull() shouldBe first
        }

        test("A timed out session fetch is an ApiError and warm-up carries on") {
            val healthy = buildSleepingPillMockEngine("/sessions.json", "/public_config_2026.json")
            val engine =
                MockEngine { request ->
                    if (request.url.encodedPath.endsWith("/session")) throw IOException("Request timeout has expired")
                    healthy.config.requestHandlers.first()(this, request)
                }
            val service = buildSleepingPillService("/sessions.json", client = buildClient(engine))

            service.warmUp()

            either { service.sessions(ConferenceId(CONFERENCE_2024_ID)) }
                .swap()
                .getOrNull()
                .shouldBeInstanceOf<SleepingPillUnreachable>()
        }
    })

private fun Collection<String>.conferenceReads() = count { it == "/data/conference" }

private fun Collection<String>.sessionReads() = count { it.endsWith("/session") }

private fun buildService(
    fixture: String,
    configFixture: String? = "/public_config_2026.json",
    clock: Clock = fixedClock("2026-10-24"),
) = buildSleepingPillService(fixture, configFixture = configFixture, clock = clock)

private suspend fun SleepingPillService.slugs(): List<String> =
    either { conferences() }.getOrNull()!!.map { it.slug }.sorted()
