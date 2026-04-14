package no.java.cupcake.sleepingpill

import arrow.core.raise.either
import arrow.core.raise.ensure
import kotlinx.serialization.Serializable
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
data class ConferenceId private constructor(
    val id: String,
) {
    companion object {
        operator fun invoke(id: String?) =
            either {
                ensure(!id.isNullOrBlank()) { ConferenceIdRequired }
                ConferenceId(id)
            }
    }
}
