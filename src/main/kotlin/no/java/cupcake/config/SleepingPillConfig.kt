package no.java.cupcake.config

data class SleepingPillConfig(
    val username: String,
    val password: String,
    val rootUrl: String,
    val cacheTtlSeconds: Long,
    val maxPastYears: Long = 3,
    val includeCurrentYear: Boolean = false,
)
