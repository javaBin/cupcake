package no.java.cupcake.bring

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

// Refreshes immediately, then waits whatever nextDelay says after each attempt. Runs until the scope is
// cancelled - launched from Application, so it stops with the server.
fun CoroutineScope.launchRefreshLoop(
    refresh: suspend () -> Boolean,
    nextDelay: (consecutiveFailures: Int) -> Duration,
): Job =
    launch {
        var consecutiveFailures = 0

        while (isActive) {
            consecutiveFailures = if (refresh()) 0 else consecutiveFailures + 1
            delay(nextDelay(consecutiveFailures))
        }
    }

private val FIRST_RETRY = 1.minutes
private val MAX_RETRY = 30.minutes

// Doubling past this can't get under MAX_RETRY anyway; capping keeps the shift from overflowing
private const val MAX_BACKOFF_EXPONENT = 5

// Once codes are loaded a failed refresh just keeps serving them, so wait the normal interval. Until the
// first load succeeds lookups return nothing, so retry sooner: 1, 2, 4, 8, 16, then every 30 minutes.
fun nextRefreshDelay(
    hasCodes: Boolean,
    consecutiveFailures: Int,
    interval: Duration,
): Duration {
    if (hasCodes || consecutiveFailures == 0) return interval

    val backoff = FIRST_RETRY * (1 shl (consecutiveFailures - 1).coerceAtMost(MAX_BACKOFF_EXPONENT))

    return minOf(backoff, MAX_RETRY, interval)
}
