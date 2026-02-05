package no.java.cupcake.api

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    @Serializable(with = HttpStatusCodeSerializer::class)
    val status: HttpStatusCode,
    val message: String,
    val fieldValue: String? = null,
)

sealed class ApiError(
    protected open val errorResponse: ErrorResponse,
) {
    open fun messageMap(): Map<String, ErrorResponse> = mapOf("error" to errorResponse)

    fun status() = this.errorResponse.status
}

abstract class UpstreamError(
    open val upstream: ErrorResponse,
    val systemName: String,
) : ApiError(
        ErrorResponse(
            status = HttpStatusCode.InternalServerError,
            message = "call to $systemName failed",
        ),
    ) {
    override fun messageMap() =
        mapOf(
            "upstream" to upstream,
            "error" to errorResponse,
        )
}

abstract class RequiredField(
    val fieldName: String,
) : ApiError(
        ErrorResponse(
            status = HttpStatusCode.BadRequest,
            message = "$fieldName required",
        ),
    )

data object ConferenceIdRequired : RequiredField(fieldName = "id")

data class SleepingPillCallFailed(
    override val upstream: ErrorResponse,
) : UpstreamError(
        upstream = upstream,
        systemName = "SleepingPill",
    )

data class SlackCallFailed(
    override val upstream: ErrorResponse,
) : UpstreamError(
        upstream = upstream,
        systemName = "Slack",
    )

data object CallPrincipalMissing : ApiError(
    ErrorResponse(
        status = HttpStatusCode.Unauthorized,
        message = "Principal missing",
    ),
)

data object TokenMissing : ApiError(
    ErrorResponse(
        status = HttpStatusCode.Unauthorized,
        message = "Principal missing token",
    ),
)

data object TokenMissingUser : ApiError(
    ErrorResponse(
        status = HttpStatusCode.Unauthorized,
        message = "User missing in token",
    ),
)

data class MissingChannelMembership(
    val channelName: String,
) : ApiError(
        ErrorResponse(
            status = HttpStatusCode.Unauthorized,
            message = "User not in correct slack channel - please ask in #kodesmia for access to $channelName",
        ),
    )

data object RefreshTokenInvalid : ApiError(
    ErrorResponse(
        status = HttpStatusCode.Unauthorized,
        message = "Refresh token is invalid or has expired",
    ),
)
