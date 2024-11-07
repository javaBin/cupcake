package no.java.cupcake.sleepingpill

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

// 2020 - invalid data due to covid
// 2007 - empty
val rejectSlugs = listOf("javazone_2020", "javazone_2007")

class SleepingPillService(
    private val client: HttpClient,
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
                title = it.data.title.value,
                description = it.data.abstractText?.value ?: "No abstract provided",
                status = Status.from(it.status),
                format = Format.from(it.data.format.value),
                language = Language.from(it.data.language.value),
                length =
                    it.data.length
                        ?.value
                        ?.toInt(),
                postcode = it.speakers.mapNotNull { speaker -> speaker.data.zipCode?.value }.joinToString(", "),
                speakers =
                    it.speakers.map { speaker ->
                        Speaker(
                            name = speaker.name,
                            email = speaker.email,
                            bio = speaker.data.bio?.value,
                            postcode = speaker.data.zipCode?.value,
                            location = speaker.data.residence?.value,
                        )
                    },
            )
        }
}
