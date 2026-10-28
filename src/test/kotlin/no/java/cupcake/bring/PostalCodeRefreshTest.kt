package no.java.cupcake.bring

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val interval = 24.hours

class PostalCodeRefreshTest :
    FunSpec({
        // The loop, with a stub policy: 1 minute after a failure, the interval after a success

        test("Refreshes immediately, retries after failures and settles on the interval after success") {
            runTest {
                val outcomes = ArrayDeque(listOf(false, false, true))
                var calls = 0

                backgroundScope.launchRefreshLoop(
                    refresh = {
                        calls++
                        outcomes.removeFirstOrNull() ?: true
                    },
                ) { failures -> if (failures > 0) 1.minutes else interval }

                runCurrent()
                calls shouldBe 1

                advanceTimeBy(1.minutes)
                runCurrent()
                calls shouldBe 2

                advanceTimeBy(1.minutes)
                runCurrent()
                calls shouldBe 3

                advanceTimeBy(interval - 1.minutes)
                runCurrent()
                calls shouldBe 3

                advanceTimeBy(1.minutes)
                runCurrent()
                calls shouldBe 4
            }
        }

        // The policy - these pin the properties agreed in the plan, not an exact curve

        test("After a success the next fetch waits the full interval") {
            nextRefreshDelay(hasCodes = true, consecutiveFailures = 0, interval = interval) shouldBe interval
        }

        test("Without codes the first retry comes well before the interval") {
            nextRefreshDelay(hasCodes = false, consecutiveFailures = 1, interval = interval) shouldBeLessThan 1.hours
        }

        test("Retries without codes back off and never wait longer than the interval") {
            val delays =
                (1..20).map {
                    nextRefreshDelay(
                        hasCodes = false,
                        consecutiveFailures = it,
                        interval = interval,
                    )
                }

            delays.zipWithNext().forEach { (a, b) -> b shouldBeGreaterThanOrEqualTo a }
            delays.last() shouldBeGreaterThan delays.first()
            delays.forEach { it shouldBeLessThanOrEqualTo interval }
        }

        test("Huge failure counts don't overflow") {
            nextRefreshDelay(
                hasCodes = false,
                consecutiveFailures = 10_000,
                interval = interval,
            ) shouldBeLessThanOrEqualTo
                interval
        }
    })
