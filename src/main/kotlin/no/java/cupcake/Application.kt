package no.java.cupcake

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.cio.EngineMain
import no.java.cupcake.bring.BringService
import no.java.cupcake.config.BringConfig
import no.java.cupcake.config.JwtConfig
import no.java.cupcake.config.SleepingPillConfig
import no.java.cupcake.clients.bringClient
import no.java.cupcake.plugins.configureHTTP
import no.java.cupcake.plugins.configureMonitoring
import no.java.cupcake.plugins.configureRouting
import no.java.cupcake.plugins.configureSecurity
import no.java.cupcake.plugins.configureSerialization
import no.java.cupcake.clients.slackBotClient
import no.java.cupcake.clients.sleepingPillClient
import no.java.cupcake.slack.SlackService
import no.java.cupcake.plugins.slackProvider
import no.java.cupcake.sleepingpill.SleepingPillService

fun main(args: Array<String>) {
    EngineMain
        .main(args)
}

fun ApplicationEnvironment.str(key: String) = this.config.property(key).getString()
fun ApplicationEnvironment.bool(key: String) = this.config.property(key).getString() == "true"

fun Application.module() {
    val bringService = BringService(
        client = bringClient(
            BringConfig(
                username = environment.str("bring.username"),
                apiKey = environment.str("bring.api_key")
            )
        ), postalCodeUrl = environment.str("bring.postalcodes_url")
    )

    val sleepingPillService = SleepingPillService(
        client = sleepingPillClient(
            SleepingPillConfig(
                username = environment.config.property("sleepingpill.username").getString(),
                password = environment.config.property("sleepingpill.password").getString(),
                rootUrl = environment.config.property("sleepingpill.base").getString(),
            )
        ), bringService = bringService
    )

    val slackProvider = slackProvider(
        clientId = environment.str("slack.client"),
        clientSecret = environment.str("slack.secret"),
        authUrl = environment.str("slack.authorize_url"),
        accessTokenUrl = environment.str("slack.accesstoken_url")
    )

    val slackService = SlackService(
        botClient = slackBotClient(
            slackBotToken = environment.config.property("slack.bot").getString()
        ),
        channel = environment.str("slack.channel"),
        membersUrl = environment.str("slack.members_url")
    )

    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSecurity(
        provider = slackProvider,
        callback = environment.str("slack.callback"),
        slackService = slackService,
        channelName = environment.str("slack.channel_name"),
        jwtConfig = JwtConfig(
            realm = environment.str("jwt.realm"),
            audience = environment.str("jwt.audience"),
            secret = environment.str("jwt.secret"),
            issuer = environment.str("jwt.issuer"),
            redirect = environment.str("jwt.redirect")
        )
    )
    configureRouting(
        sleepingPillService = sleepingPillService,
        securityOptional = !environment.bool("jwt.enabled")
    )
}
