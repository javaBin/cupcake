package no.java.cupcake.sleepingpill

import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import no.java.cupcake.api.ConferenceIdRequired
import no.java.cupcake.buildSleepingPillService
import no.java.cupcake.fixedClock
import no.java.cupcake.randomString
import java.time.Clock

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
                    service.sessions(ConferenceId(randomString()))
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
    })

private fun buildService(
    fixture: String,
    configFixture: String? = "/public_config_2026.json",
    clock: Clock = fixedClock("2026-10-24"),
) = buildSleepingPillService(fixture, configFixture = configFixture, clock = clock)

private suspend fun SleepingPillService.slugs(): List<String> =
    either { conferences() }.getOrNull()!!.map { it.slug }.sorted()
