package no.java.cupcake.sleepingpill

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import no.java.cupcake.bring.BringService

// 2020 - invalid data due to covid
// 2007 - empty
val rejectSlugs = listOf("javazone_2020", "javazone_2007")

class SleepingPillService(
    private val client: HttpClient,
    private val bringService: BringService
) {
    suspend fun conferences() =
        client
            .get("/data/conference")
            .body<SleepingPillConferences>()
            .conferences
            .filterNot { rejectSlugs.contains(it.slug) }
            .map {
                Conference(
                    name = it.name,
                    slug = it.slug,
                    id = it.id,
                )
            }

    suspend fun sessions(id: String) =
        client.get("/data/conference/$id/session").body<SleepingPillSessions>().sessions.map {
            Session(
                id = it.id,
                title = it.data.title.value,
                description = it.data.abstractText?.value ?: "No abstract provided",
                status = Status.from(it.status),
                format = Format.from(it.data.format.value),
                language = Language.from(it.data.language.value),
                length =
                    it.data.length
                        ?.value
                        ?.toInt(),
                speakers =
                    it.speakers.map { speaker ->
                        val code = bringService.getPostalCode(speaker.data.zipCode?.value)

                        Speaker(
                            name = speaker.name,
                            email = speaker.email,
                            bio = speaker.data.bio?.value,
                            postcode = speaker.data.zipCode?.value,
                            location = speaker.data.residence?.value,
                            city = code?.city,
                            county = code?.county
                        )
                    },
            )
        }
}
