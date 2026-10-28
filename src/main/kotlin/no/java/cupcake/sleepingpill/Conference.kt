package no.java.cupcake.sleepingpill

import arrow.core.raise.Raise
import arrow.core.raise.context.ensureNotNull
import kotlinx.serialization.Serializable
import no.java.cupcake.api.ApiError
import no.java.cupcake.api.ConferenceIdRequired
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter

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
    // Null for names that don't end in a year; those conferences are skipped rather than failing the list
    val year: Year? get() = name.substringAfterLast(" ").toIntOrNull()?.let(Year::of)
}

@Serializable
data class SleepingPillConferences(
    val conferences: List<SleepingPillConference>,
)

private val publicConfigDateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")

@Serializable
data class SleepingPillPublicConfig(
    val conferenceName: String,
    val conferenceDates: List<String>,
) {
    val year: Year get() = Year.of(conferenceName.substringAfterLast(" ").toInt())

    val lastDay: LocalDate get() = conferenceDates.maxOf { LocalDate.parse(it, publicConfigDateFormat) }

    // The latest year whose conference is over: this one once its last day has passed, otherwise the one before
    fun completedYear(today: LocalDate): Year = if (lastDay.isBefore(today)) year else year.minusYears(1)
}

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
