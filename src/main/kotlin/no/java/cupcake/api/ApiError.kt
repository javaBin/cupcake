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

sealed interface ApiError {
    val response: ErrorResponse
}

fun ApiError.status() = response.status

fun ApiError.messageMap(): Map<String, ErrorResponse> =
    when (this) {
        is UpstreamError -> mapOf("upstream" to upstream, "error" to response)

        is RequiredField,
        is CallPrincipalMissing,
        is TokenMissing,
        is TokenMissingUser,
        is RefreshTokenInvalid,
        -> mapOf("error" to response)
    }

abstract class UpstreamError(
    open val upstream: ErrorResponse,
    val systemName: String,
) : ApiError {
    override val response =
        ErrorResponse(
            status = HttpStatusCode.InternalServerError,
            message = "call to $systemName failed",
        )
}

abstract class RequiredField(
    val fieldName: String,
) : ApiError {
    override val response =
        ErrorResponse(
            status = HttpStatusCode.BadRequest,
            message = "$fieldName required",
        )
}

data object ConferenceIdRequired : RequiredField(fieldName = "id")

data class SleepingPillCallFailed(
    override val upstream: ErrorResponse,
) : UpstreamError(
        upstream = upstream,
        systemName = "SleepingPill",
    )

data class CognitoCallFailed(
    override val upstream: ErrorResponse,
) : UpstreamError(
        upstream = upstream,
        systemName = "Cognito",
    )

data object CallPrincipalMissing : ApiError {
    override val response =
        ErrorResponse(
            status = HttpStatusCode.Unauthorized,
            message = "Principal missing",
        )
}

data object TokenMissing : ApiError {
    override val response =
        ErrorResponse(
            status = HttpStatusCode.Unauthorized,
            message = "Principal missing token",
        )
}

data object TokenMissingUser : ApiError {
    override val response =
        ErrorResponse(
            status = HttpStatusCode.Unauthorized,
            message = "User missing in token",
        )
}

data object RefreshTokenInvalid : ApiError {
    override val response =
        ErrorResponse(
            status = HttpStatusCode.Unauthorized,
            message = "Refresh token is invalid or has expired",
        )
}
