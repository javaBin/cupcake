package no.java.cupcake.plugins

import arrow.core.raise.either
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.java.cupcake.api.respond
import no.java.cupcake.sleepingpill.ConferenceId
import no.java.cupcake.sleepingpill.SleepingPillService

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
                        either {
                            sleepingPillService.conferences(raise = this).sortedByDescending { it.name }
                        }.respond(this)
                    }

                    route("/{id}") {
                        get("/sessions") {
                            either {
                                sleepingPillService.sessions(
                                    id = ConferenceId(call.parameters["id"]).bind(),
                                    raise = this,
                                )
                            }.respond(this)
                        }
                    }
                }
            }
        }
    }
}
