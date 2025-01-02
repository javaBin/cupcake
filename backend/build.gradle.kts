plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinter)
    alias(libs.plugins.detekt)
    alias(libs.plugins.versions)
    alias(libs.plugins.serialization)
    alias(libs.plugins.dependency.analysis)
    jacoco
}

group = "no.java.cupcake"
version = "0.0.1"

kotlin {
    jvmToolchain(22)
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

    implementation(libs.kotlin.logging)
    implementation(libs.micrometer.registry.prometheus)

    runtimeOnly(libs.logback.classic)

    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.kotlin.test.junit)
}

tasks.shadowJar {
    archiveFileName.set("cupcake.jar")
}

tasks.jar {
    enabled = false
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}