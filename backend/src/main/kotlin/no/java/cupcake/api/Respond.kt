package no.java.cupcake.api

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext

suspend inline fun <reified A : Any> Either<ApiError, A>.performResponse(
    context: RoutingContext,
    status: HttpStatusCode = HttpStatusCode.OK,
    redirect: Boolean = false,
) = when (this) {
    is Either.Left -> context.respond(value)
    is Either.Right ->
        when (redirect) {
            false -> context.call.respond(status, value)
            true -> context.call.respondRedirect(value.toString())
        }
}

suspend inline fun <reified A : Any> Either<ApiError, A>.respond(
    context: RoutingContext,
    status: HttpStatusCode = HttpStatusCode.OK,
) = performResponse(context, status, redirect = false)

suspend inline fun <reified A : Any> Either<ApiError, A>.redirect(context: RoutingContext) =
    performResponse(context, redirect = true)

suspend fun RoutingContext.respond(error: ApiError) = call.respond(error.status(), error.messageMap())
