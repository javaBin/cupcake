package no.java.cupcake.slack

import arrow.core.Either
import arrow.core.raise.either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.java.cupcake.api.ApiError
import no.java.cupcake.buildSlackService

class SlackServiceTest :
    FunSpec({
        test("Channel member is in channel") {
            val service = buildService()

            val isMember: Either<ApiError, Boolean> =
                either {
                    service.isMember("testUser1", raise = this)
                }

            isMember.isRight() shouldBe true
            isMember.getOrNull() shouldBe true
        }

        test("Non-channel member is not in channel") {
            val service = buildService()

            val isMember: Either<ApiError, Boolean> =
                either {
                    service.isMember("banana", raise = this)
                }

            isMember.isRight() shouldBe true
            isMember.getOrNull() shouldBe false
        }
    })

private fun buildService() =
    buildSlackService(
        fixture = "/slack_members.json",
        channel = "TestChannel",
        membersUrl = "/test",
    )
