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
import no.java.cupcake.api.ApiError
import no.java.cupcake.api.BringCallFailed
import no.java.cupcake.api.BringResponseUnreadable
import no.java.cupcake.api.ErrorResponse

private val logger = KotlinLogging.logger {}

private const val MAX_ERROR_BODY = 500

// Holds the postal code lookup; when to call refresh() is decided by the refresh loop in PostalCodeRefresh.kt
class BringService(
    private val client: HttpClient,
    private val postalCodeUrl: String,
) {
    // Swapped as a whole on each successful refresh, so a failed refresh keeps the previous codes
    @Volatile
    private var postalCodes: Map<String, PostalCode> = emptyMap()

    val hasCodes: Boolean get() = postalCodes.isNotEmpty()

    // Returns whether the refresh succeeded
    suspend fun refresh(): Boolean {
        logger.info { "Refreshing postal code cache" }

        return either { fetchPostalCodes() }
            .onRight { codes ->
                postalCodes = codes.associateBy { it.postalCode }
                logger.info { "Loaded ${codes.size} postal codes" }
            }.onLeft { logger.warn { "Postal code refresh failed, keeping ${postalCodes.size} cached codes: $it" } }
            .isRight()
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
}
