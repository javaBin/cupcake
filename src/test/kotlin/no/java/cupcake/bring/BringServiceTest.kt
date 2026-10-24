package no.java.cupcake.bring

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import no.java.cupcake.buildClient
import no.java.cupcake.buildErrorMockEngine
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

        test("Postal code with surrounding whitespace is found") {
            val service = buildService()

            service.getPostalCode(" 1555 ")?.city shouldBe "Son"
        }

        test("Failed refresh does not throw and returns no postal codes") {
            val service = buildService(HttpStatusCode.Unauthorized, "Authentication failed")

            service.getPostalCode("1555") shouldBe null
        }
    })

private fun buildService(
    status: HttpStatusCode = HttpStatusCode.OK,
    errorBody: String = "",
): BringService =
    BringService(
        client =
            buildClient(
                if (status.isSuccess()) {
                    MockEngine { _ ->
                        respond(
                            content = ByteReadChannel(loadFixture("/postal_codes.json")),
                            status = status,
                            headers = headersOf(HttpHeaders.ContentType, "application/json"),
                        )
                    }
                } else {
                    buildErrorMockEngine(status, errorBody)
                },
            ),
        postalCodeUrl = "/test",
        scheduler = false,
    )
