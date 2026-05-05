package no.java.cupcake.sleepingpill

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import kotlinx.serialization.Serializable
import no.java.cupcake.api.ApiError
import no.java.cupcake.api.ConferenceIdRequired
import java.time.Year

@Serializable
data class Conference(
    val name: String,
    val slug: String,
    val id: String,
)

@Serializable
data class SleepingPillConference(
    val name: String,
    val slug: String,
    val id: String,
    val slottimes: String?,
) {
    val year: Year get() = Year.of(name.substringAfterLast(" ").toInt())
}

@Serializable
data class SleepingPillConferences(
    val conferences: List<SleepingPillConference>,
)

@Serializable
@JvmInline
value class ConferenceId private constructor(
    val id: String,
) {
    companion object {
        context(_: Raise<ApiError>)
        operator fun invoke(id: String?) =
            ConferenceId(ensureNotNull(id?.takeIf { it.isNotBlank() }) { ConferenceIdRequired })
    }
}
