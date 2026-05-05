package no.java.cupcake.api

import arrow.core.Either
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.RoutingContext

context(context: RoutingContext)
suspend inline fun <reified A : Any> Either<ApiError, A>.respond(status: HttpStatusCode = HttpStatusCode.OK) {
    onLeft { context.respond(it) }
    onRight { context.call.respond(status, it) }
}

context(context: RoutingContext)
suspend inline fun <reified A : Any> Either<ApiError, A>.redirect() {
    onLeft { context.respond(it) }
    onRight { context.call.respondRedirect(it.toString()) }
}

suspend fun RoutingContext.respond(error: ApiError) = call.respond(error.status(), error.messageMap())
