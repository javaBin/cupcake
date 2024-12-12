package no.java.cupcake.bring

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import no.java.cupcake.buildClient
import no.java.cupcake.loadFixture

class BringServiceTest :
    FunSpec({
        test("Empty query returns null") {
            val service = buildService()

            val postalCode = service.getPostalCode(null)

            postalCode shouldBe null
        }

        test("Known postalcode returns correct information") {
            val service = buildService()

            val postalCode = service.getPostalCode("1555")

            postalCode shouldBe
                PostalCode(
                    postalCode = "1555",
                    city = "Son",
                    municipalityId = "3019",
                    municipality = "Vestby",
                    county = "Viken",
                    poBox = false,
                    latitude = "59.5237",
                    longitude = "10.6862",
                    special = null,
                )
        }

        test("Unknown postalcode returns null") {
            val service = buildService()

            val postalCode = service.getPostalCode("0000")

            postalCode shouldBe null
        }
    })

private fun buildService(): BringService =
    BringService(
        client =
            buildClient(
                MockEngine { request ->
                    respond(
                        content = ByteReadChannel(loadFixture("/postal_codes.json")),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json"),
                    )
                },
            ),
        postalCodeUrl = "/test",
        scheduler = false,
    )
