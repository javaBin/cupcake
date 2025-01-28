package no.java.cupcake.slack

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import no.java.cupcake.api.ApiError
import no.java.cupcake.api.ErrorResponse
import no.java.cupcake.api.SlackCallFailed

private val logger = KotlinLogging.logger {}

class SlackService(
    private val botClient: HttpClient,
    private val channel: String,
    private val membersUrl: String,
) {
    private suspend fun getChannelMembers(raise: Raise<ApiError>) =
        botClient
            .get(membersUrl) {
                logger.info { "Getting channel members for channel $channel" }

                url {
                    parameters.append("channel", channel)
                }
            }.valid(raise)
            .body<SlackMembers>()

    suspend fun isMember(
        id: String,
        raise: Raise<ApiError>,
    ) = getChannelMembers(raise).members?.contains(id) == true

    private suspend fun HttpResponse.valid(raise: Raise<ApiError>): HttpResponse {
        raise.ensure(this.status.isSuccess()) {
            logger.warn { "Failed to fetch channel members from slack - ${this.status}" }

            SlackCallFailed(ErrorResponse(this.status, this.bodyAsText()))
        }

        return this
    }
}
