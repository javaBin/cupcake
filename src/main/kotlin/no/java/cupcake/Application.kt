package no.java.cupcake

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.cio.EngineMain
import no.java.cupcake.bring.BringService
import no.java.cupcake.clients.bringClient
import no.java.cupcake.clients.sleepingPillClient
import no.java.cupcake.plugins.configureAuth
import no.java.cupcake.plugins.configureHTTP
import no.java.cupcake.plugins.configureMonitoring
import no.java.cupcake.plugins.configureRouting
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.plugins.configureUserInfoRoute
import no.java.cupcake.sleepingpill.SleepingPillService

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun ApplicationEnvironment.str(key: String) = this.config.property(key).getString()

fun ApplicationEnvironment.bool(key: String) = this.config.property(key).getString() == "true"

fun ApplicationEnvironment.long(key: String) =
    this.config
        .property(key)
        .getString()
        .toLong()

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureAuth(
        oidcConfig = environment.oidcConfig(),
    )
    configureUserInfoRoute(oidcConfig = environment.oidcConfig())
    configureRouting(
        sleepingPillService = sleepingPillService(bringService()),
        securityOptional = !environment.bool("jwt.enabled"),
    )
}

private fun Application.sleepingPillService(bringService: BringService): SleepingPillService {
    val config = environment.sleepingPillConfig()

    return SleepingPillService(
        client = sleepingPillClient(config),
        bringService = bringService,
        cacheTimeoutSeconds = config.cacheTtlSeconds,
        maxPastYears = config.maxPastYears,
        includeCurrentYear = config.includeCurrentYear,
    )
}

private fun Application.bringService(): BringService =
    BringService(
        client = bringClient(environment.bringConfig()),
        postalCodeUrl = environment.str("bring.postalcodes_url"),
    )
