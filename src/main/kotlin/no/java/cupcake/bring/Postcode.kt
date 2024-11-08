package no.java.cupcake.bring

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostalCode(
    @SerialName("postal_code")
    val postalCode: String,
    val city: String,
    val municipalityId: String?,
    val municipality: String?,
    val county: String?,
    @SerialName("po_box")
    val poBox: Boolean,
    val latitude: String?,
    val longitude: String?,
    val special: Boolean?,
)

@Serializable
data class Navigation(
    @SerialName("total_hits")
    val totalHits: Int,
    @SerialName("self")
    val selfUrl: String,
)

@Serializable
data class PostalCodes(
    val navigation: Navigation,
    @SerialName("postal_codes")
    val postalCodes: List<PostalCode>,
)