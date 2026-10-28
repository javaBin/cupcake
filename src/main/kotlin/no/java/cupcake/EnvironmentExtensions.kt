package no.java.cupcake

import io.ktor.server.application.ApplicationEnvironment
import no.java.cupcake.config.BringConfig
import no.java.cupcake.config.OidcConfig
import no.java.cupcake.config.SleepingPillConfig

fun ApplicationEnvironment.bringConfig() =
    BringConfig(
        username = str("bring.username"),
        apiKey = str("bring.api_key"),
        postalCodesUrl = str("bring.postalcodes_url"),
        refreshHours = long("bring.refresh_hours"),
    )

fun ApplicationEnvironment.sleepingPillConfig() =
    SleepingPillConfig(
        username = str("sleepingpill.username"),
        password = str("sleepingpill.password"),
        rootUrl = str("sleepingpill.base"),
        cacheTtlSeconds = long("sleepingpill.cache_ttl_seconds"),
        maxPastYears = long("sleepingpill.max_past_years"),
    )

fun ApplicationEnvironment.oidcConfig() =
    OidcConfig(
        wellKnownUrl = str("oidc.well_known_url"),
    )
