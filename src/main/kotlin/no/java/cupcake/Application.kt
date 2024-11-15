package no.java.cupcake

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import no.java.cupcake.bring.BringService
import no.java.cupcake.plugins.bringClient
import no.java.cupcake.plugins.configureHTTP
import no.java.cupcake.plugins.configureMonitoring
import no.java.cupcake.plugins.configureRouting
import no.java.cupcake.plugins.configureSecurity
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.plugins.slackBotClient
import no.java.cupcake.plugins.sleepingPillClient
import no.java.cupcake.slack.SlackService
import no.java.cupcake.plugins.slackProvider
import no.java.cupcake.sleepingpill.SleepingPillService

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain
        .main(args)
}

fun ApplicationEnvironment.str(key: String) = this.config.property(key).getString()

fun Application.module() {
    val bringService = BringService(bringClient())
    val sleepingPillService = SleepingPillService(sleepingPillClient(), bringService)
    val slackProvider = slackProvider(environment.str("slack.client"), environment.str("slack.secret"))
    val slackService = SlackService(slackBotClient(), environment.str("slack.channel"))

    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity(
        provider = slackProvider,
        callback = environment.str("slack.callback"),
        slackService = slackService,
        channelName = environment.str("slack.channel_name")
    )
    configureRouting(sleepingPillService)
}
