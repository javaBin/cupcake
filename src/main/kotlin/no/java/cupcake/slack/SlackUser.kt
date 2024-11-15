package no.java.cupcake.slack

import kotlinx.serialization.Serializable

@Serializable
data class SlackUser(
    val userId: String,
    val email: String,
    val name: String,
    val avatar: String,
    val member: Boolean
)