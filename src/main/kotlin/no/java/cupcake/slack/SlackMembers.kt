package no.java.cupcake.slack

import kotlinx.serialization.Serializable

@Serializable
data class SlackMembers(
    val ok: Boolean,
    val members: List<String>?,
    val error: String?
)
