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
)

@Serializable
data class Session(
    val title: String,
    @SerialName("abstract") val description: String,
    val status: Status,
    val format: Format,
    val language: Language,
    val length: Int?,
    val postcode: String,
    val speakers: List<Speaker>,
)

@Serializable
data class SleepingPillSession(
    val postedBy: String?,
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
    val twitter: PrivateValue<String>?,
    val bio: PrivateValue<String>?,
    @SerialName("zip-code") val zipCode: PrivateValue<String>?,
    val residence: PrivateValue<String>?,
)

@Serializable
data class SleepingPillTalk(
    val participation: PrivateValue<String>?,
    val intendedAudience: PrivateValue<String>?,
    val outline: PrivateValue<String>?,
    val suggestedKeywords: PrivateValue<String>?,
    val length: PrivateValue<String>?,
    val format: PrivateValue<String>,
    val infoToProgramCommittee: PrivateValue<String>?,
    val equipment: PrivateValue<String>?,
    val language: PrivateValue<String>,
    @SerialName("abstract") var abstractText: PrivateValue<String>?,
    val title: PrivateValue<String>,
    // var pkomfeedbacks : FeedbacksData,
    val tagswithauthor: PrivateValue<List<SleepingPillTag>>?,
    val tags: PrivateValue<List<String>>?,
    // var feedback : FeedbackSummary? = null // Optional field if present in some objects only.
)

@Serializable
data class SleepingPillTag(
    val author: String,
    val tag: String,
)

@Serializable
data class PrivateValue<T>(
    val privateData: Boolean,
    val value: T,
)

/*
@Serializable
data class FeedbacksData(
    val privateData: Boolean,  // Similar structure as above for feedbacks.
    val value: ArrayList<Feedback>
)

@Serializable
data class Feedback(
    val author: String,
    val created: String,
    val id: String,
    val talkid: String,
    val feedbacktype: String,
    val info: String
)

@Serializable
data class TagsWithAuthorData(val privateData: Boolean, val value: ArrayList<Tag>)
 */
