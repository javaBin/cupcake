package no.java.cupcake.config

data class JwtConfig(
    val realm: String,
    val audience: String,
    val secret: String,
    val issuer: String,
    val redirect: String,
)
