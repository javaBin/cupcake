package no.java.cupcake.sleepingpill

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.context.ensure
import arrow.core.raise.context.raise
import arrow.core.raise.either
import arrow.core.raise.nullable
import com.github.benmanes.caffeine.cache.AsyncLoadingCache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.future.await
import no.java.cupcake.api.ApiError
import no.java.cupcake.api.ConferenceNotFound
import no.java.cupcake.api.ErrorResponse
import no.java.cupcake.api.SleepingPillCallFailed
import no.java.cupcake.api.SleepingPillConfigUnreadable
import no.java.cupcake.api.SleepingPillUnreachable
import no.java.cupcake.bring.BringService
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId

private val logger = KotlinLogging.logger {}

// 2020 - invalid data due to covid
// 2007 - empty
val rejectSlugs = listOf("javazone_2020", "javazone_2007")

// Rejected, year-less and out-of-window conferences are skipped, not request failures
private fun SleepingPillConference.toConference(window: ClosedRange<Year>): Conference? =
    nullable {
        ensure(slug !in rejectSlugs)
        ensure(year.bind() in window)

        Conference(name = name, slug = slug, id = id)
    }

// Only known conference ids reach the cache, so this is a backstop rather than the real bound
private const val MAX_CACHED_CONFERENCES = 50L

// TTL ≤ 0 disables refreshing; the ticker is the caches' clock, which tests move forward
data class CacheSettings(
    val ttlSeconds: Long,
    val ticker: Ticker = Ticker.systemTicker(),
)

class SleepingPillService(
    private val client: HttpClient,
    private val bringService: BringService,
    private val cache: CacheSettings,
    private val maxPastYears: Long,
    private val clock: Clock = Clock.system(ZoneId.of("Europe/Oslo")),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val ttl: Duration = if (cache.ttlSeconds <= 0) Duration.ZERO else Duration.ofSeconds(cache.ttlSeconds)
    private val cacheScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatcher)

    // refreshAfterWrite serves the cached value past the TTL while one background reload runs; a failed
    // reload keeps the old value. SleepingPill is read at most once per key per TTL and no request waits on it.
    private fun cacheBuilder(): Caffeine<Any, Any> =
        Caffeine.newBuilder().ticker(cache.ticker).apply { if (!ttl.isZero) refreshAfterWrite(ttl) }

    private val conferencesCache: AsyncLoadingCache<String, List<Conference>> =
        cacheBuilder().buildAsync { _, _ ->
            cacheScope
                .async {
                    uncachedConferencesEither().getOrElse { throw ApiErrorException(it) }
                }.asCompletableFuture()
        }

    private val sessionsCache: AsyncLoadingCache<ConferenceId, List<Session>> =
        cacheBuilder().maximumSize(MAX_CACHED_CONFERENCES).buildAsync { id, _ ->
            cacheScope
                .async {
                    uncachedSessionsEither(id).getOrElse { throw ApiErrorException(it) }
                }.asCompletableFuture()
        }

    // Sequential on purpose: SleepingPill is slow to serve large session lists and parallel fetches time out
    suspend fun warmUp() {
        either { conferences() }
            .onLeft { logger.warn { "Failed to warm conference cache: $it" } }
            .getOrElse { emptyList() }
            .forEach { conference ->
                either { sessions(ConferenceId(conference.id)) }
                    .onLeft { logger.warn { "Failed to warm sessions for ${conference.name}: $it" } }
            }
    }

    private suspend fun cutoffYear(): Year =
        either {
            catch({
                client
                    .get("/public/config")
                    .valid()
                    .body<SleepingPillPublicConfig>()
                    .completedYear(LocalDate.now(clock))
            }) { raise(SleepingPillConfigUnreadable(it)) }
        }.getOrElse {
            logger.warn { "Could not determine last completed conference, falling back to last year: $it" }
            Year.now(clock).minusYears(1)
        }

    private suspend fun uncachedConferencesEither(): Either<ApiError, List<Conference>> =
        either {
            val cutoff = cutoffYear()
            val window = cutoff.minusYears(maxPastYears - 1)..cutoff

            reachable {
                client
                    .get("/data/conference")
                    .valid()
                    .body<SleepingPillConferences>()
                    .conferences
                    .mapNotNull { it.toConference(window) }
            }
        }

    private suspend fun uncachedSessionsEither(id: ConferenceId): Either<ApiError, List<Session>> =
        either {
            reachable {
                client
                    .get("/data/conference/${id.id}/session")
                    .valid()
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
                                    ?.toIntOrNull(),
                            speakers =
                                it.speakers.map { speaker ->
                                    Speaker(
                                        name = speaker.name,
                                        email = speaker.email,
                                        bio = speaker.data.bio?.value,
                                        postcode = speaker.data.zipCode?.value,
                                        location = speaker.data.residence?.value,
                                        city = null,
                                        county = null,
                                    )
                                },
                        )
                    }
            }
        }

    // Timeouts and connection failures are exceptions, not ApiErrors; without this they escape Raise
    context(_: Raise<ApiError>)
    private suspend fun <T> reachable(block: suspend () -> T): T =
        catch({ block() }) { raise(SleepingPillUnreachable(it)) }

    private class ApiErrorException(
        val error: ApiError,
    ) : RuntimeException()

    context(_: Raise<ApiError>)
    private suspend fun HttpResponse.valid(): HttpResponse {
        ensure(this.status.isSuccess()) {
            logger.warn { "Failed to fetch information from sleeping pill - ${this.status}" }
            SleepingPillCallFailed(ErrorResponse(this.status, this.bodyAsText()))
        }
        return this
    }

    context(_: Raise<ApiError>)
    private suspend fun <T> getOrRaise(block: suspend () -> T): T =
        try {
            block()
        } catch (e: ApiErrorException) {
            raise(e.error)
        }

    context(_: Raise<ApiError>)
    suspend fun conferences(): List<Conference> = getOrRaise { conferencesCache.get("all").await() }

    // Unknown ids are rejected before the cache, so arbitrary URLs can't trigger SleepingPill reads
    context(_: Raise<ApiError>)
    suspend fun sessions(id: ConferenceId): List<Session> {
        ensure(conferences().any { it.id == id.id }) { ConferenceNotFound(id.id) }

        return getOrRaise { sessionsCache.get(id).await() }.map { it.withPostalCodes() }
    }

    // Looked up per request, not cached with the session, so late-loaded postal codes apply at once
    private fun Session.withPostalCodes(): Session =
        copy(
            speakers =
                speakers.map { speaker ->
                    val code = bringService.getPostalCode(speaker.postcode)
                    speaker.copy(city = code?.city, county = code?.county)
                },
        )
}
