package no.java.cupcake.sleepingpill

import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.java.cupcake.api.ConferenceIdRequired
import no.java.cupcake.buildSleepingPillService
import no.java.cupcake.randomString

class SleepingPillServiceTest :
    FunSpec({
        test("Can fetch conference list") {
            val service = buildService("/conferences.json")

            val conferences =
                either {
                    service.conferences(raise = this)
                }

            conferences.isRight() shouldBe true

            val conferenceList = conferences.getOrNull()!!

            conferenceList.size shouldBe 17

            val conference2024 = conferenceList.first { it.slug == "javazone_2024" }

            conference2024 shouldBe
                Conference(
                    name = "Javazone 2024",
                    slug = "javazone_2024",
                    id = "ad82e461-9444-40a4-a9d5-cc4885f9107a",
                )

            rejectSlugs.forEach { slug ->
                conferenceList.filter { it.slug == slug } shouldBe emptyList()
            }
        }

        test("Can fetch session list") {
            val service = buildService("/sessions.json")

            val sessions =
                either {
                    service.sessions(ConferenceId(randomString()).bind(), raise = this)
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
                    service.sessions(ConferenceId(null).bind(), raise = this)
                }

            sessions.isRight() shouldBe false
            sessions.swap().getOrNull()!! shouldBe ConferenceIdRequired
        }
    })

private fun buildService(fixture: String) = buildSleepingPillService(fixture)
