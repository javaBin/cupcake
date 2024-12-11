package no.java.cupcake.sleepingpill

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import no.java.cupcake.bring.BringService
import no.java.cupcake.buildClient
import no.java.cupcake.buildSleepingPillService
import no.java.cupcake.loadFixture
import no.java.cupcake.randomString

class SleepingPillServiceTest : FunSpec({
    test("Can fetch conference list") {
        val service = buildService("/conferences.json")

        val conferences = service.conferences()

        conferences.size shouldBe 17

        val conference2024 = conferences.first { it.slug == "javazone_2024" }

        conference2024 shouldBe Conference(
            name = "Javazone 2024",
            slug = "javazone_2024",
            id = "ad82e461-9444-40a4-a9d5-cc4885f9107a"
        )

        rejectSlugs.forEach { slug ->
            conferences.filter { it.slug == slug } shouldBe emptyList()
        }
    }

    test("Can fetch session list") {
        val service = buildService("/sessions.json")

        val sessions = service.sessions(randomString())

        sessions.size shouldBe 1

        with(sessions.first()) {
            id shouldBe "57f8dbb5-af4b-453f-b0c2-14067aae21b8"
            title shouldBe "Test talk 2024"
            description shouldBe "My test description"
            status shouldBe Status.SUBMITTED
            format shouldBe Format.PRESENTATION
            language shouldBe Language.NORWEGIAN
            length shouldBe 60
            speakers shouldBe listOf(
                Speaker(
                    name = "Test Testerson",
                    email = "test@gmail.com",
                    bio = "Hello I am me",
                    postcode = "1555",
                    location = "Norway",
                    city = "Son",
                    county = "Viken"
                )
            )
        }
    }
})

private fun buildService(fixture: String) = buildSleepingPillService(fixture)
