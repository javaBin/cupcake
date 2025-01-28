package no.java.cupcake.config

data class SlackConfig(
    val clientId: String,
    val clientSecret: String,
    val authUrl: String,
    val accessTokenUrl: String,
)
