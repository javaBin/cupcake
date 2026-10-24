package no.java.cupcake.bring

import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import arrow.core.raise.either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.java.cupcake.api.ApiError
import no.java.cupcake.api.BringCallFailed
import no.java.cupcake.api.BringResponseUnreadable
import no.java.cupcake.api.ErrorResponse
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.time.Duration.Companion.hours

val logger = KotlinLogging.logger {}

private const val MAX_ERROR_BODY = 500

class BringService(
    private val client: HttpClient,
    private val postalCodeUrl: String,
    scheduler: Boolean = true,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    // Swapped as a whole on each successful refresh, so a failed refresh keeps the previous codes
    @Volatile
    private var postalCodes: Map<String, PostalCode> = emptyMap()

    init {
        if (scheduler) {
            scheduleRefresh()
        } else {
            runBlocking {
                refresh()
            }
        }
    }

    private suspend fun refresh() {
        logger.info { "Refreshing postal code cache" }

        either { fetchPostalCodes() }
            .onRight { codes ->
                postalCodes = codes.associateBy { it.postalCode }
                logger.info { "Loaded ${codes.size} postal codes" }
            }.onLeft { logger.warn { "Postal code refresh failed, keeping ${postalCodes.size} cached codes: $it" } }
    }

    context(_: Raise<ApiError>)
    private suspend fun fetchPostalCodes(): List<PostalCode> =
        catch({
            val response = client.get(postalCodeUrl)

            ensure(response.status.isSuccess()) {
                BringCallFailed(ErrorResponse(response.status, response.bodyAsText().take(MAX_ERROR_BODY)))
            }

            response.body<PostalCodes>().postalCodes
        }) { raise(BringResponseUnreadable(it)) }

    fun getPostalCode(id: String?) = id?.trim()?.let(postalCodes::get)

    private fun scheduleRefresh() {
        logger.info { "Scheduling postal code cache refresh" }

        Timer().scheduleAtFixedRate(
            timerTask {
                logger.info { "Running scheduled refresh" }

                val scope = CoroutineScope(dispatcher)

                scope.launch {
                    refresh()
                }
            },
            0L,
            1.hours.inWholeMilliseconds,
        )

        logger.info { "Scheduler setup complete" }
    }
}
