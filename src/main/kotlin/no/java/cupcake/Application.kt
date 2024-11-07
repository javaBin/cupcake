package no.java.cupcake

import io.ktor.server.application.Application
import no.java.cupcake.plugins.configureHTTP
import no.java.cupcake.plugins.configureMonitoring
import no.java.cupcake.plugins.configureRouting
import no.java.cupcake.plugins.configureSecurity
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.plugins.sleepingPillClient
import no.java.cupcake.sleepingpill.SleepingPillService

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain
        .main(args)
}

fun Application.module() {
    val client = sleepingPillClient()
    val sleepingPillService = SleepingPillService(client)

    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity()
    configureRouting(sleepingPillService)
}
