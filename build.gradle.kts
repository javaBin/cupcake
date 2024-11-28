plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    // https://github.com/jeremymailen/kotlinter-gradle/issues/414
    // alias(libs.plugins.kotlinter)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.serialization)
}

group = "no.java.cupcake"
version = "0.0.1"

kotlin {
    jvmToolchain(22)

    compilerOptions {
        freeCompilerArgs = listOf("-Xconsistent-data-class-copy-visibility")
    }
}

application {
    mainClass.set("no.java.cupcake.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.ktor.client)

    implementation(libs.cache4k)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)
    implementation(libs.micrometer.registry.prometheus)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

tasks.shadowJar {
    archiveFileName.set("cupcake.jar")
}

tasks.jar {
    enabled = false
}
