package no.java.cupcake.slack

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

class SlackServiceTest : FunSpec({
    test("Channel member is in channel") {
        val service = buildService()

        val isMember = service.isMember("testUser1")

        isMember shouldBe true
    }

    test("Non-channel member is not in channel") {
        val service = buildService()

        val isMember = service.isMember("banana")

        isMember shouldBe false
    }
})

private fun buildService(): SlackService = SlackService(
    botClient = buildClient(MockEngine { request ->
        respond(
            content = ByteReadChannel(loadFixture("/slack_members.json")),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }),
    channel = "TestChannel",
    membersUrl = "/test"
)
