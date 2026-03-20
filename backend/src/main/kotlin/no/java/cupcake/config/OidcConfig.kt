package no.java.cupcake.config

data class OidcConfig(
    val wellKnownUrl: String,
    val expectedAzp: String,
)
