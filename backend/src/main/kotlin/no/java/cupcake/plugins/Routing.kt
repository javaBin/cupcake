package no.java.cupcake.plugins

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.java.cupcake.sleepingpill.SleepingPillService

private val logger = KotlinLogging.logger {}

fun Application.configureRouting(
    sleepingPillService: SleepingPillService,
    securityOptional: Boolean,
) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    routing {
        authenticate(JWT_AUTH, optional = securityOptional) {
            route("/api") {
                route("/conferences") {
                    get {
                        val conferences = sleepingPillService.conferences().sortedByDescending { it.name }

                        call.respond(conferences)
                    }

                    route("/{id}") {
                        get("/sessions") {
                            val conferenceId =
                                call.parameters["id"] ?: throw BadRequestException("Conference ID required")

                            val sessions = sleepingPillService.sessions(conferenceId)

                            call.respond(sessions)
                        }
                    }
                }
            }
        }
    }
}
