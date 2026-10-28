package no.java.cupcake

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.cio.EngineMain
import io.ktor.server.request.path
import kotlinx.coroutines.launch
import no.java.cupcake.bring.BringService
import no.java.cupcake.bring.launchRefreshLoop
import no.java.cupcake.bring.nextRefreshDelay
import no.java.cupcake.clients.bringClient
import no.java.cupcake.clients.oidcClient
import no.java.cupcake.clients.sleepingPillClient
import no.java.cupcake.plugins.configureAuth
import no.java.cupcake.plugins.configureHTTP
import no.java.cupcake.plugins.configureMonitoring
import no.java.cupcake.plugins.configureRouting
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.plugins.configureUserInfoRoute
import no.java.cupcake.sleepingpill.CacheSettings
import no.java.cupcake.sleepingpill.SleepingPillService
import kotlin.time.Duration.Companion.hours

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

// Infrastructure endpoints answer while the caches warm up; everything else waits for warm-up
private val ungatedPaths = setOf("/health", "/metrics-micrometer")

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    val oidcClient = oidcClient()
    val userInfoEndpoint = configureAuth(oidcConfig = environment.oidcConfig(), http = oidcClient)
    configureUserInfoRoute(userInfoEndpoint = userInfoEndpoint, http = oidcClient)
    val sleepingPillService = sleepingPillService(bringService())
    configureRouting(
        sleepingPillService = sleepingPillService,
        securityOptional = !environment.bool("jwt.enabled"),
    )
    val ready = launch { sleepingPillService.warmUp() }
    intercept(ApplicationCallPipeline.Setup) {
        if (context.request.path() !in ungatedPaths) ready.join()
    }
}

private fun Application.sleepingPillService(bringService: BringService): SleepingPillService {
    val config = environment.sleepingPillConfig()

    return SleepingPillService(
        client = sleepingPillClient(config),
        bringService = bringService,
        cache = CacheSettings(ttlSeconds = config.cacheTtlSeconds),
        maxPastYears = config.maxPastYears,
    )
}

private fun Application.bringService(): BringService {
    val config = environment.bringConfig()
    val interval = config.refreshHours.hours

    return BringService(
        client = bringClient(config),
        postalCodeUrl = config.postalCodesUrl,
    ).also { bring ->
        launchRefreshLoop(refresh = bring::refresh) { failures ->
            nextRefreshDelay(hasCodes = bring.hasCodes, consecutiveFailures = failures, interval = interval)
        }
    }
}
