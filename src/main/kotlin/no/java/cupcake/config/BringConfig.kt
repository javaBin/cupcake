package no.java.cupcake.config

data class BringConfig(
    val username: String,
    val apiKey: String,
    val postalCodesUrl: String,
    val refreshHours: Long,
)
