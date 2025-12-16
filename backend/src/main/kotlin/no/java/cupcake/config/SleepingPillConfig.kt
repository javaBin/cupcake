package no.java.cupcake.config

data class SleepingPillConfig(
    val username: String,
    val password: String,
    val rootUrl: String,
    val cacheTtlSeconds: Long,
)
