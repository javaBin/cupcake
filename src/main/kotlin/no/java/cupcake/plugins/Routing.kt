package no.java.cupcake.plugins

import arrow.core.raise.either
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.java.cupcake.api.InternalError
import no.java.cupcake.api.messageMap
import no.java.cupcake.api.respond
import no.java.cupcake.api.status
import no.java.cupcake.sleepingpill.ConferenceId
import no.java.cupcake.sleepingpill.SleepingPillService

private val logger = KotlinLogging.logger {}

fun Application.configureRouting(
    sleepingPillService: SleepingPillService,
    securityOptional: Boolean,
) {
    install(StatusPages) {
        // The cause goes to the log only; exception text can carry internals the client shouldn't see
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unhandled exception on ${call.request.path()}" }
            call.respond(InternalError.status(), InternalError.messageMap())
        }
    }

    routing {
        get("/health") {
            call.respondText("OK")
        }

        route("/api") {
            authenticate("javaBin", optional = securityOptional) {
                route("/conferences") {
                    get {
                        either {
                            sleepingPillService.conferences().sortedByDescending { it.name }
                        }.respond()
                    }

                    route("/{id}") {
                        get("/sessions") {
                            either {
                                sleepingPillService.sessions(
                                    id = ConferenceId(call.parameters["id"]),
                                )
                            }.respond()
                        }
                    }
                }
            }
        }
    }
}
