package no.java.cupcake

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.cio.EngineMain
import no.java.cupcake.bring.BringService
import no.java.cupcake.clients.bringClient
import no.java.cupcake.clients.slackBotClient
import no.java.cupcake.clients.sleepingPillClient
import no.java.cupcake.plugins.configureHTTP
import no.java.cupcake.plugins.configureMonitoring
import no.java.cupcake.plugins.configureRouting
import no.java.cupcake.plugins.configureSecurity
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.plugins.slackProvider
import no.java.cupcake.slack.SlackService
import no.java.cupcake.sleepingpill.SleepingPillService

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun ApplicationEnvironment.str(key: String) = this.config.property(key).getString()

fun ApplicationEnvironment.bool(key: String) = this.config.property(key).getString() == "true"

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity(
        provider = slackProvider(),
        callback = environment.str("slack.callback"),
        slackService = slackService(),
        channelName = environment.str("slack.channel_name"),
        jwtConfig = environment.jwtConfig(),
    )
    configureRouting(
        sleepingPillService = sleepingPillService(bringService()),
        securityOptional = !environment.bool("jwt.enabled"),
    )
}

private fun Application.slackService(): SlackService =
    SlackService(
        botClient =
            slackBotClient(
                slackBotToken = environment.config.property("slack.bot").getString(),
            ),
        channel = environment.str("slack.channel"),
        membersUrl = environment.str("slack.members_url"),
    )

private fun Application.slackProvider(): OAuthServerSettings.OAuth2ServerSettings =
    slackProvider(
        clientId = environment.str("slack.client"),
        clientSecret = environment.str("slack.secret"),
        authUrl = environment.str("slack.authorize_url"),
        accessTokenUrl = environment.str("slack.accesstoken_url"),
    )

private fun Application.sleepingPillService(bringService: BringService): SleepingPillService =
    SleepingPillService(
        client = sleepingPillClient(environment.sleepingPillConfig()),
        bringService = bringService,
    )

private fun Application.bringService(): BringService =
    BringService(
        client = bringClient(environment.bringConfig()),
        postalCodeUrl = environment.str("bring.postalcodes_url"),
    )
