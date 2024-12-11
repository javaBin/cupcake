package no.java.cupcake.slack

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.java.cupcake.buildSlackService

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

private fun buildService() = buildSlackService(
    fixture = "/slack_members.json",
    channel = "TestChannel",
    membersUrl = "/test"
)
