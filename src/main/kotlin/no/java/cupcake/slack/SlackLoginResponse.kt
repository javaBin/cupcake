package no.java.cupcake.slack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SlackLoginResponse(
    val ok: Boolean,
    @SerialName("authed_user") val user: SlackUser?,
    val error: String?,
)

@Serializable
data class SlackUser(
    val id: String,
    @SerialName("access_token") val token: String,
)
