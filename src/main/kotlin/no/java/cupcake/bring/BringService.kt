package no.java.cupcake.bring

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import  io.github.reactivecircus.cache4k.Cache
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.time.Duration.Companion.hours

val logger = KotlinLogging.logger {}

class BringService(
    private val client: HttpClient,
    private val postalCodeUrl: String,
    scheduler: Boolean = true
) {
    private val cache = Cache.Builder<String, PostalCode>().expireAfterWrite(24.hours).build()

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

        cache.invalidateAll()

        val codes = client.get(postalCodeUrl).body<PostalCodes>()

        codes.postalCodes.forEach { postalCode ->
            cache.put(postalCode.postalCode, postalCode)
        }
    }

    fun getPostalCode(id: String?): PostalCode? {
        if (id == null) return null

        return cache.get(id)
    }

    private fun scheduleRefresh() {
        logger.info { "Scheduling postal code cache refresh" }

        Timer().scheduleAtFixedRate(
            timerTask {
                logger.info { "Running scheduled refresh" }

                val scope = CoroutineScope(Dispatchers.IO)

                scope.launch {
                    refresh()
                }
            },
            0L,
            1.hours.inWholeMilliseconds
        )

        logger.info { "Scheduler setup complete" }
    }
}
