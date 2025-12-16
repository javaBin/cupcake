package no.java.cupcake.sleepingpill

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.await
import no.java.cupcake.api.ApiError
import no.java.cupcake.api.ErrorResponse
import no.java.cupcake.api.SleepingPillCallFailed
import no.java.cupcake.bring.BringService
import java.time.Duration

private val logger = KotlinLogging.logger {}

// 2020 - invalid data due to covid
// 2007 - empty
val rejectSlugs = listOf("javazone_2020", "javazone_2007")

class SleepingPillService(
    private val client: HttpClient,
    private val bringService: BringService,
    cacheTimeoutSeconds: Long,
    private val cacheScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) {
    private val ttl: Duration =
        if (cacheTimeoutSeconds <= 0) Duration.ZERO else Duration.ofSeconds(cacheTimeoutSeconds)

    private val conferencesCache: AsyncLoadingCache<String, List<Conference>> =
        Caffeine
            .newBuilder()
            .apply { if (!ttl.isZero) expireAfterWrite(ttl) }
            .buildAsync { _, _ ->
                cacheScope
                    .async {
                        uncachedConferencesEither().fold(
                            ifLeft = { throw ApiErrorException(it) },
                            ifRight = { it },
                        )
                    }.asCompletableFuture()
            }

    private val sessionsCache: AsyncLoadingCache<ConferenceId, List<Session>> =
        Caffeine
            .newBuilder()
            .apply { if (!ttl.isZero) expireAfterWrite(ttl) }
            .buildAsync { id, _ ->
                cacheScope
                    .async {
                        uncachedSessionsEither(id).fold(
                            ifLeft = { throw ApiErrorException(it) },
                            ifRight = { it },
                        )
                    }.asCompletableFuture()
            }

    private suspend fun uncachedConferencesEither(): Either<ApiError, List<Conference>> =
        either {
            client
                .get("/data/conference")
                .valid(this)
                .body<SleepingPillConferences>()
                .conferences
                .filterNot { rejectSlugs.contains(it.slug) }
                .map {
                    Conference(
                        name = it.name,
                        slug = it.slug,
                        id = it.id,
                    )
                }
        }

    private suspend fun uncachedSessionsEither(id: ConferenceId): Either<ApiError, List<Session>> =
        either {
            client
                .get("/data/conference/${id.id}/session")
                .valid(this)
                .body<SleepingPillSessions>()
                .sessions
                .map {
                    Session(
                        id = it.id,
                        title = it.data.title.value,
                        description = it.data.abstractText?.value ?: "No abstract provided",
                        status = Status.from(it.status),
                        format = Format.from(it.data.format.value),
                        language = Language.from(it.data.language.value),
                        length =
                            it.data.length
                                ?.value
                                ?.toInt(),
                        speakers =
                            it.speakers.map { speaker ->
                                val code = bringService.getPostalCode(speaker.data.zipCode?.value)
                                Speaker(
                                    name = speaker.name,
                                    email = speaker.email,
                                    bio = speaker.data.bio?.value,
                                    postcode = speaker.data.zipCode?.value,
                                    location = speaker.data.residence?.value,
                                    city = code?.city,
                                    county = code?.county,
                                )
                            },
                    )
                }
        }

    private class ApiErrorException(
        val error: ApiError,
    ) : RuntimeException()

    private suspend fun HttpResponse.valid(raise: Raise<ApiError>): HttpResponse {
        raise.ensure(this.status.isSuccess()) {
            logger.warn { "Failed to fetch information from sleeping pill - ${this.status}" }
            SleepingPillCallFailed(ErrorResponse(this.status, this.bodyAsText()))
        }
        return this
    }

    context(raise: Raise<ApiError>)
    private suspend fun <T> getOrRaise(block: suspend () -> T): T =
        try {
            block()
        } catch (e: ApiErrorException) {
            raise.raise(e.error)
        }

    context(raise: Raise<ApiError>)
    suspend fun conferences(): List<Conference> = getOrRaise { conferencesCache.get("all").await() }

    context(raise: Raise<ApiError>)
    suspend fun sessions(id: ConferenceId): List<Session> = getOrRaise { sessionsCache.get(id).await() }
}
