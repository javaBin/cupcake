package no.java.cupcake.sleepingpill

import kotlinx.serialization.Serializable

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
)

@Serializable
data class SleepingPillConferences(
    val conferences: List<SleepingPillConference>,
)
