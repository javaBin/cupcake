package no.java.cupcake.sleepingpill

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Status {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED,
    HISTORIC,
    UNKNOWN,
    ;

    companion object {
        fun from(status: String) = entries.firstOrNull { it.name == status.uppercase() } ?: UNKNOWN
    }
}

enum class Format(
    val format: String,
) {
    LIGHTNING_TALK("lightning-talk"),
    PRESENTATION("presentation"),
    WORKSHOP("workshop"),
    PANEL("panel"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun from(format: String) = entries.firstOrNull { it.format.uppercase() == format.uppercase() } ?: UNKNOWN
    }
}

enum class Language(
    val language: String,
) {
    ENGLISH("en"),
    NORWEGIAN("no"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun from(language: String) = entries.firstOrNull { it.language.uppercase() == language.uppercase() } ?: UNKNOWN
    }
}

@Serializable
data class Speaker(
    val name: String,
    val email: String,
    val bio: String?,
    val postcode: String?,
    val location: String?,
    val city: String?,
    val county: String?,
)

@Serializable
data class Session(
    val id: String,
    val title: String,
    @SerialName("abstract") val description: String,
    val status: Status,
    val format: Format,
    val language: Language,
    val length: Int?,
    val speakers: List<Speaker>,
)

// Only fields cupcake uses are modelled; unknown keys are ignored, so other fields can't break parsing
@Serializable
data class SleepingPillSession(
    val speakers: List<SleepingPillSpeaker>,
    val id: String,
    val data: SleepingPillTalk,
    val status: String,
)

@Serializable
data class SleepingPillSessions(
    val sessions: List<SleepingPillSession>,
)

@Serializable
data class SleepingPillSpeaker(
    val name: String,
    val id: String,
    val data: SleepingPillSpeakerData,
    val email: String,
)

@Serializable
data class SleepingPillSpeakerData(
    val bio: PrivateValue<String>?,
    @SerialName("zip-code") val zipCode: PrivateValue<String>?,
    val residence: PrivateValue<String>?,
)

@Serializable
data class SleepingPillTalk(
    val length: PrivateValue<String>?,
    val format: PrivateValue<String>,
    val language: PrivateValue<String>,
    @SerialName("abstract") val abstractText: PrivateValue<String>?,
    val title: PrivateValue<String>,
)

@Serializable
data class PrivateValue<T>(
    val privateData: Boolean,
    val value: T,
)
