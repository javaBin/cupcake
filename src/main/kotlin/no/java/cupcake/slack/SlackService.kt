package no.java.cupcake.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private val logger = KotlinLogging.logger {}

class SlackService(private val botClient: HttpClient, private val channel: String, private val membersUrl: String) {
    private suspend fun getChannelMembers() =
        botClient.get(membersUrl) {
            logger.info { "Getting channel members for channel $channel" }

            url {
                parameters.append("channel", channel)
            }
        }.body<SlackMembers>()

    suspend fun isMember(id: String) = getChannelMembers().members?.contains(id) == true
}

